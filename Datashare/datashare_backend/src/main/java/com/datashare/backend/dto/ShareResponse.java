package com.datashare.backend.dto;

/**
 * Réponse renvoyée après un partage réussi.
 */
public record ShareResponse(
        String message,
        String recipientEmail,
        String downloadUrl,
        String downloadToken,
        String fileName
) {}
