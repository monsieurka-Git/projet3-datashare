package com.datashare.backend.repository;

import com.datashare.backend.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour gérer les fichiers.
 */
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    // Historique des fichiers (US05)
    List<FileEntity> findByOwnerId(UUID ownerId);

    // Suppression automatique (US01)
    List<FileEntity> findByExpiresAtBefore(Instant now);

    // US02 — Téléchargement via token unique
    Optional<FileEntity> findByDownloadToken(String token);
}
