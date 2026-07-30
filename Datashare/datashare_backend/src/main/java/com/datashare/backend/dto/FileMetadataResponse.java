package com.datashare.backend.dto;

import lombok.*;

import java.time.Instant;

/**
 * DTO renvoyé avant le téléchargement.
 * Conforme à US02 :
 * - Métadonnées visibles avant téléchargement
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileMetadataResponse {

    private String originalName;
    private long size;
    private String contentType;
    private Instant expiresAt;
    private boolean passwordProtected;
}
