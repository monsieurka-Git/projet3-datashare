package com.datashare.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions pour l'API DataShare.
 * Convertit les exceptions métier en codes HTTP appropriés avec des messages clairs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les RuntimeException (exceptions métier non contrôlées).
     * 
     * Cas traités :
     * - "Lien invalide ou introuvable" → HTTP 404
     * - "Lien expiré" → HTTP 410 Gone
     * - "Mot de passe requis" → HTTP 400
     * - "Mot de passe incorrect" → HTTP 401
     * - Autres → HTTP 500
     */
    @ExceptionHandler(RuntimeException.class)
public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    String message = ex.getMessage();
    log.warn("⚠️ Exception métier: {}", message);

    HttpStatus status;
    if (message != null) {
        if (message.contains("introuvable") || message.contains("invalide")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (message.contains("expiré")) {
            status = HttpStatus.GONE; // 410
        } else if (message.contains("requis")) {
            status = HttpStatus.BAD_REQUEST; // 400
        } else if (message.contains("incorrect")) {
            status = HttpStatus.UNAUTHORIZED; // 401
        } else if (message.contains("supprimer") || message.contains("propres fichiers")) {
            status = HttpStatus.FORBIDDEN; // 403
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }
    } else {
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return ResponseEntity.status(status).body(Map.of(
            "error", message != null ? message : "Erreur interne du serveur",
            "timestamp", Instant.now().toString()
    ));
}


    /**
     * Gère les exceptions générales non prévues.
     * Retourne HTTP 500 avec un message générique.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("❌ Erreur non gérée: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erreur interne du serveur",
                "timestamp", Instant.now().toString()
        ));
    }
}