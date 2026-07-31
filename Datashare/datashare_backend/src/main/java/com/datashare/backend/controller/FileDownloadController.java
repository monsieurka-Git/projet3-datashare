package com.datashare.backend.controller;

import com.datashare.backend.dto.FileMetadataResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur gérant le téléchargement des fichiers via token unique (US02).
 * 
 * Deux endpoints accessibles SANS authentification JWT :
 * - GET  /api/files/metadata/{token}  → Affiche les métadonnées avant téléchargement
 * - POST /api/files/download/{token}  → Télécharge le fichier (mot de passe requis si protégé)
 * 
 * ✅ Test avec Postman (metadata) :
 *    GET http://localhost:8080/api/files/metadata/<token>
 *    (Sans authentification)
 * 
 * ✅ Test avec Postman (download) :
 *    POST http://localhost:8080/api/files/download/<token>
 *    Body : form-data ou x-www-form-urlencoded
 *      - Key : "password" (optionnel)
 *    (Sans authentification)
 * 
 * ✅ Test avec Swagger :
 *    Les endpoints sont publics, pas besoin de token JWT
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    /**
     * US02 — Affichage des métadonnées avant téléchargement.
     * Accessible à tout utilisateur (SANS authentification JWT).
     * 
     * ⚠️ Contrairement au téléchargement effectif, cette méthode NE vérifie PAS
     * le mot de passe. L'utilisateur peut voir les infos du fichier avant de
     * décider de le télécharger (et éventuellement saisir le mot de passe).
     * 
     * @param token Token unique de téléchargement
     * @return Métadonnées du fichier (nom, taille, type, expiration, protection)
     */
    @GetMapping("/metadata/{token}")
    public ResponseEntity<FileMetadataResponse> getMetadata(@PathVariable String token) {

        log.debug("📄 Consultation des métadonnées pour le token : {}", token);

        // ✅ Utilise getFileMetadata() qui ne vérifie PAS le mot de passe
        //    (contrairement à getFileForDownload() utilisé pour le téléchargement effectif)
        FileEntity file = fileDownloadService.getFileMetadata(token);

        // Construction du DTO de réponse avec les métadonnées
        FileMetadataResponse response = FileMetadataResponse.builder()
                .originalName(file.getOriginalName())                    // Nom original du fichier
                .size(file.getSize())                                    // Taille en octets
                .contentType(file.getContentType())                      // Type MIME
                .expiresAt(file.getExpiresAt())                          // Date d'expiration du lien
                .passwordProtected(file.getDownloadPasswordHash() != null) // true si mot de passe requis
                .build();

        log.debug("✅ Métadonnées renvoyées pour : {}", file.getOriginalName());
        return ResponseEntity.ok(response);
    }

    /**
     * US02 — Téléchargement effectif du fichier via token unique.
     * Accessible à tout utilisateur (SANS authentification JWT).
     * 
     * Si le fichier est protégé par mot de passe, le paramètre "password"
     * est obligatoire dans le corps de la requête (form-data).
     * 
     * @param token    Token unique de téléchargement
     * @param password Mot de passe (optionnel, requis si le fichier est protégé)
     * @return Le fichier en pièce jointe (Content-Disposition: attachment)
     * @throws Exception si le fichier est introuvable ou inaccessible
     */
    @PostMapping("/download/{token}")
    public ResponseEntity<byte[]> download(
            @PathVariable String token,
            @RequestParam(value = "password", required = false) String password
    ) throws Exception {

        log.debug("📥 Demande de téléchargement pour le token : {}", token);

        // ✅ Vérification du token, expiration ET mot de passe (si protégé)
        FileEntity file = fileDownloadService.getFileForDownload(token, password);

        // 📂 Chargement du fichier physique depuis le disque
        byte[] data = fileDownloadService.loadFileBytes(file.getFilename());

        log.debug("✅ Téléchargement réussi : {} ({} octets)", file.getOriginalName(), data.length);

        // Retourne le fichier en pièce jointe avec le nom original
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(data);
    }
}
