package com.datashare.backend.dto;

import lombok.*;
import java.time.Instant;

/**
 * DTO représentant un élément de l'historique des fichiers (US05).
 * Contient :
 * - nom du fichier
 * - taille
 * - date d'envoi
 * - date d'expiration
 * - état du lien (valide ou expiré)
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileHistoryResponse {

    private String originalName;
    private long size;
    private Instant uploadedAt;
    private Instant expiresAt;
    private boolean expired; // true = lien expiré, false = lien valide
}
