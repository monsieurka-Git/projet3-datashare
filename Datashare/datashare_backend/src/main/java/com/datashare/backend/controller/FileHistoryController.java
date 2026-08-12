package com.datashare.backend.controller;

import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.service.FileHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur gérant l'historique des fichiers (US05).
 * Accessible uniquement à l'utilisateur connecté (JWT requis).
 * 
 * ✅ Test avec Postman :
 *    GET http://localhost:8080/api/files/history
 *    Header : Authorization: Bearer <token_JWT>
 * 
 * ✅ Test avec Swagger :
 *    Cliquer sur "Authorize" et coller le token JWT
 *    Utiliser l'endpoint GET /api/files/history
 */
@RestController
@RequestMapping("/api/files")
public class FileHistoryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileHistoryController.class);

    private final FileHistoryService fileHistoryService;

    public FileHistoryController(FileHistoryService fileHistoryService) {
        this.fileHistoryService = fileHistoryService;
    }

    /**
     * US05 — Consultation de l'historique des fichiers de l'utilisateur connecté.
     * 
     * @param auth Authentification Spring Security (injectée automatiquement via le JWT)
     * @return Liste des fichiers uploadés par l'utilisateur, avec leurs métadonnées
     */
    @GetMapping("/history")
    public ResponseEntity<List<FileHistoryResponse>> getHistory(Authentication auth) {

        // 🔐 Vérification obligatoire : l'utilisateur doit être authentifié
        if (auth == null || auth.getName() == null) {
            log.warn("⚠️  Tentative d'accès à l'historique sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 👤 Récupération de l'UUID de l'utilisateur depuis le JWT
        //    auth.getName() contient l'UUID positionné par JwtFilter
        UUID userId = UUID.fromString(auth.getName());
        log.debug("📋 Consultation de l'historique pour l'utilisateur : {}", userId);

        // 📦 Appel du service qui récupère les fichiers et les transforme en DTO
        List<FileHistoryResponse> history = fileHistoryService.getUserHistory(userId);

        return ResponseEntity.ok(history);
    }
}
