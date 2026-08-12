package com.datashare.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404ForInvalidLink() {
        RuntimeException ex = new RuntimeException("Lien invalide ou introuvable");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn410ForExpiredLink() {
        RuntimeException ex = new RuntimeException("Lien expiré");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }

    @Test
    void shouldReturn403ForUnauthorizedDelete() {
        RuntimeException ex = new RuntimeException("Vous ne pouvez supprimer que vos propres fichiers");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturn500ForGenericException() {
        Exception ex = new Exception("Erreur interne");
        ResponseEntity<Map<String, Object>> response = handler.handleException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
