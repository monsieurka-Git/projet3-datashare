package com.datashare.backend.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.dto.FileDto;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur principal de gestion des fichiers (upload, informations, suppression).
 * 
 * Endpoints protégés par JWT (sauf indication contraire) :
 * - POST /api/files/upload   → Upload d'un fichier
 * - GET  /api/files/info/{id} → Informations d'un fichier
 * - DELETE /api/files/info/{id} → Suppression d'un fichier (propriétaire uniquement)
 * 
 * ✅ Test avec Postman (upload) :
 *    POST http://localhost:8080/api/files/upload
 *    Header : Authorization: Bearer <token_JWT>
 *    Body : form-data → Key "file" (type File)
 * 
 * ✅ Test avec Swagger :
 *    Cliquer sur "Authorize" et coller le token JWT
 *    Utiliser les endpoints POST/GET/DELETE /api/files/...
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * ✅ POST /api/files/upload
     * Upload d'un fichier. Nécessite un token JWT valide.
     * 
     * @param file Le fichier à uploader (multipart/form-data)
     * @param auth Authentification Spring Security (injectée automatiquement)
     * @return L'entité FileEntity sauvegardée avec ses métadonnées
     * @throws IOException En cas d'erreur d'écriture sur le disque
     */
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileEntity> upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {

        log.debug("📥 Requête d'upload reçue : {}", file.getOriginalFilename());

        // 🔐 Vérifie que l'utilisateur est authentifié (sécurité supplémentaire)
        if (auth == null || auth.getName() == null) {
            log.warn("⚠️  Tentative d'upload sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 👤 Récupère l'UUID de l'utilisateur depuis l'authentification
        //    auth.getName() = UUID positionné par JwtFilter
        UUID userId = UUID.fromString(auth.getName());
        log.debug("👤 Upload par l'utilisateur : {}", userId);

        // ✅ Upload du fichier via le service (sauvegarde disque + base de données)
        FileEntity saved = fileService.uploadFile(file, userId);

        log.debug("✅ Upload réussi, ID du fichier : {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    /**
     * ✅ GET /api/files/info/{id}
     * Récupère les informations d'un fichier par son ID.
     * Nécessite un token JWT valide.
     * 
     * @param id   UUID du fichier
     * @param auth Authentification Spring Security
     * @return FileDto contenant les informations du fichier (id, filename, downloadToken, ownerId)
     */
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getFile(@PathVariable UUID id, Authentication auth) {

        log.debug("📄 Consultation des informations du fichier : {}", id);

        // 🔐 Vérifie que l'utilisateur est authentifié
        if (auth == null || auth.getName() == null) {
            log.warn("⚠️  Tentative d'accès aux infos sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 🔍 Recherche du fichier en base de données
        FileEntity file = fileService.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️  Fichier introuvable : {}", id);
                    return new RuntimeException("Fichier introuvable avec l'ID : " + id);
                });

        // Construction du DTO de réponse
        FileDto dto = new FileDto(
            file.getId(),
            file.getFilename(),
            file.getDownloadToken(),
            file.getOwnerId()
        );

        log.debug("✅ Informations renvoyées pour le fichier : {}", file.getOriginalName());
        return ResponseEntity.ok(dto);
    }

    /**
     * ✅ DELETE /api/files/info/{id}
     * US06 — Suppression d'un fichier par son propriétaire.
     * 
     * Règles appliquées :
     * - L'utilisateur doit être connecté (JWT valide)
     * - L'utilisateur ne peut supprimer que ses propres fichiers
     * - Suppression physique du fichier sur le disque + suppression en base
     * - Action irréversible
     * 
     * @param id   UUID du fichier à supprimer
     * @param auth Authentification Spring Security
     * @return HTTP 204 No Content si la suppression réussit
     */
    @DeleteMapping("/info/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth) {

        log.debug("🗑 Demande de suppression du fichier : {}", id);

        // 🔐 Vérifie que l'utilisateur est authentifié
        if (auth == null || auth.getName() == null) {
            log.warn("⚠️  Tentative de suppression sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 👤 Récupère l'UUID de l'utilisateur depuis le JWT
        UUID userId = UUID.fromString(auth.getName());
        log.debug("👤 Suppression demandée par l'utilisateur : {}", userId);

        // 🗑 Suppression via le service (vérifie le propriétaire, supprime disque + base)
        // Les exceptions sont gérées par GlobalExceptionHandler
        fileService.deleteFileForUser(id, userId);
        log.debug("✅ Fichier {} supprimé avec succès", id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
