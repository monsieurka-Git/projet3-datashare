package com.datashare.backend.exception;

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
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        log.warn("Exception metier: {}", message);

        HttpStatus status;
        if (message != null) {
            String m = message.toLowerCase();
            if (m.contains("introuvable") || m.contains("invalide")) {
                status = HttpStatus.NOT_FOUND;
            } else if (m.contains("expiré") || m.contains("expire")) {
                status = HttpStatus.GONE;
            } else if (m.contains("incorrect")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (
                    m.contains("interdit")
                    || m.contains("taille")
                    || m.contains("maximum")
                    || m.contains("requis")
                    || m.contains("mot de passe")
                    || m.contains("expiration")
                    || m.contains("manquant")
            ) {
                // Validations US01 (extension, taille, password, etc.) → 400
                status = HttpStatus.BAD_REQUEST;
            } else if (m.contains("supprimer") || m.contains("propres fichiers") || m.contains("partager")) {
                status = HttpStatus.FORBIDDEN;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(Map.of(
                "error", message != null ? message : "Erreur interne du serveur",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Erreur non geree: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erreur interne du serveur",
                "timestamp", Instant.now().toString()
        ));
    }
}
