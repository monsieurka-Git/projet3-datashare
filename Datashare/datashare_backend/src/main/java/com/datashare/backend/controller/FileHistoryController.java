package com.datashare.backend.controller;

import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.service.FileHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur gérant l'historique des fichiers (US05).
 * Accessible uniquement à l'utilisateur connecté.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileHistoryController {

    private final FileHistoryService fileHistoryService;

    /**
     * US05 — Consultation de l'historique.
     * Retourne tous les fichiers envoyés par l'utilisateur.
     */
    @GetMapping("/history")
    public ResponseEntity<List<FileHistoryResponse>> getHistory(Authentication auth) {

        // Récupération de l'ID utilisateur depuis le JWT
        UUID userId = UUID.fromString(auth.getName());

        // Appel du service
        List<FileHistoryResponse> history = fileHistoryService.getUserHistory(userId);

        return ResponseEntity.ok(history);
    }
}
