package com.datashare.backend.controller;

import java.io.IOException;
import java.util.List;
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
import com.datashare.backend.dto.UploadDto;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileService;


@RestController
@RequestMapping("/api/files")
@org.springframework.web.bind.annotation.CrossOrigin(
        originPatterns = {"http://localhost:4200", "http://127.0.0.1:4200"},
        allowCredentials = "true"
)
public class FileController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /api/files/upload
     * Upload d'un fichier avec options avancées.
     * Renvoie désormais un DTO contenant le lien de téléchargement.
     */
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UploadDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiresInDays", required = false) Integer expiresInDays,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "tags", required = false) String tags,
            Authentication auth) throws IOException {

        log.debug("Requête d'upload reçue : {}", file.getOriginalFilename());

        if (auth == null || auth.getName() == null) {
            log.warn("Tentative d'upload sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(auth.getName());
        log.debug("Upload par l'utilisateur : {}", userId);

        // Upload réel
        FileEntity saved = fileService.uploadFile(file, userId, expiresInDays, password, tags);

        log.debug("Upload réussi, ID du fichier : {}", saved.getId());

        // DTO contenant le token de téléchargement.
        // On renvoie aussi une downloadUrl relative vers la page frontend /download/:token
        // (le frontend la reconstruit de toute façon avec window.location.origin,
        // mais on fournit une valeur utile pour les clients qui s'en servent).
        // Ne JAMAIS renvoyer l'URL de l'API POST /api/files/download/{token} :
        // ouverte en GET dans un navigateur elle répond 405.
        String token = saved.getDownloadToken();
        String frontendDownloadUrl = (token != null) ? "/download/" + token : null;

        UploadDto dto = new UploadDto(
            saved.getId(),
            saved.getFilename(),
            saved.getOriginalName(),
            token,
            frontendDownloadUrl
        );

        log.info("Upload OK – id={}, token={}, downloadUrl={}",
                saved.getId(), token, frontendDownloadUrl);

        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/files/info/{id}
     * Renvoie les métadonnées d'un fichier.
     */
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getFile(@PathVariable UUID id, Authentication auth) {

        log.debug("📄 Consultation des informations du fichier : {}", id);

        if (auth == null || auth.getName() == null) {
            log.warn("⚠️ Tentative d'accès aux infos sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileEntity file = fileService.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Fichier introuvable : {}", id);
                    return new RuntimeException("Fichier introuvable avec l'ID : " + id);
                });

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
     * POST /api/files/upload/anonymous
     * US07 — Upload anonyme (sans compte).
     * Mêmes règles que US01, mais ownerId = null (pas d'historique).
     * Tags interdits (US08 réservé aux connectés).
     */
    @PostMapping(
        value = "/upload/anonymous",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UploadDto> uploadAnonymous(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiresInDays", required = false) Integer expiresInDays,
            @RequestParam(value = "password", required = false) String password) throws IOException {

        log.info("US07 — Upload anonyme : {}", file.getOriginalFilename());

        // userId = null → fichier non lié à un compte
        FileEntity saved = fileService.uploadFile(file, null, expiresInDays, password, null);

        String token = saved.getDownloadToken();
        String frontendDownloadUrl = (token != null) ? "/download/" + token : null;

        UploadDto dto = new UploadDto(
                saved.getId(),
                saved.getFilename(),
                saved.getOriginalName(),
                token,
                frontendDownloadUrl
        );

        log.info("US07 Upload anonyme OK — token={}", token);
        return ResponseEntity.ok(dto);
    }

    /**
     * DELETE /api/files/info/{id}
     * Suppression d'un fichier par son propriétaire.
     */
    @DeleteMapping("/info/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication auth) {

        log.debug("🗑 Demande de suppression du fichier : {}", id);

        if (auth == null || auth.getName() == null) {
            log.warn("⚠️ Tentative de suppression sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(auth.getName());
        log.debug("👤 Suppression demandée par l'utilisateur : {}", userId);

        fileService.deleteFileForUser(id, userId);
        log.debug("✅ Fichier {} supprimé avec succès", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/files
     * Liste des fichiers de l'utilisateur connecté.
     */
    @GetMapping
    public ResponseEntity<?> getUserFiles(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            log.warn("⚠️ Tentative d'accès à la liste des fichiers sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(auth.getName());
        log.debug("👤 Récupération des fichiers de l'utilisateur : {}", userId);

        List<FileEntity> files = fileService.findByUser(userId);
        log.debug("✅ {} fichier(s) trouvé(s) pour l'utilisateur {}", files.size(), userId);

        return ResponseEntity.ok(files);
    }

    /**
     * PUT /api/files/{id}/tags
     * US08 — Met à jour les tags d'un fichier (utilisateur connecté uniquement).
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}/tags")
    public ResponseEntity<FileEntity> updateTags(
            @PathVariable UUID id,
            @RequestParam("tags") String tags,
            Authentication auth) {

        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = UUID.fromString(auth.getName());
        FileEntity updated = fileService.updateTags(id, userId, tags);
        return ResponseEntity.ok(updated);
    }

}
