package com.datashare.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFound() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleRuntimeException(new RuntimeException("Lien invalide ou introuvable"));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void mapsExpired() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleRuntimeException(new RuntimeException("Lien expiré"));
        assertEquals(HttpStatus.GONE, res.getStatusCode());
    }

    @Test
    void mapsBadRequestForSizeAndForbiddenExt() {
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleRuntimeException(new RuntimeException("La taille maximale autorisée est de 1 Go")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleRuntimeException(new RuntimeException("Type de fichier interdit : .exe")).getStatusCode());
    }

    @Test
    void mapsUnauthorizedPassword() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleRuntimeException(new RuntimeException("Mot de passe incorrect")).getStatusCode());
    }

    @Test
    void mapsForbiddenDelete() {
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleRuntimeException(new RuntimeException("Vous ne pouvez supprimer que vos propres fichiers")).getStatusCode());
    }

    @Test
    void mapsGenericException() {
        ResponseEntity<Map<String, Object>> res = handler.handleException(new Exception("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
    }
}
