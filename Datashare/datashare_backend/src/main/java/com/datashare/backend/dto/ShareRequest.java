package com.datashare.backend.dto;

/**
 * Corps de la requête POST /api/share
 */
public record ShareRequest(
        String fileId,
        String recipientEmail
) {}
