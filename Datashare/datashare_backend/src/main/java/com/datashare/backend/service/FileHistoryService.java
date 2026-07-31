package com.datashare.backend.service;

import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service gérant la consultation de l'historique (US05).
 * Règles :
 * - Accessible uniquement à l'utilisateur connecté
 * - Aucun tri ni filtrage obligatoire
 */
@Service
@RequiredArgsConstructor
public class FileHistoryService {

    private final FileRepository fileRepository;

    /**
     * Retourne l'historique des fichiers d'un utilisateur.
     */
    public List<FileHistoryResponse> getUserHistory(UUID userId) {

        // Récupération des fichiers appartenant à l'utilisateur
        List<FileEntity> files = fileRepository.findByOwnerId(userId);

        // Transformation en DTO
        return files.stream().map(file -> {

            // Gestion du cas où expiresAt est null (anciens fichiers)
            Instant expiresAt = file.getExpiresAt();
            boolean expired = (expiresAt == null) || expiresAt.isBefore(Instant.now());


            return FileHistoryResponse.builder()
                    .originalName(file.getOriginalName())
                    .size(file.getSize())
                    .uploadedAt(file.getCreatedAt())   // 🔥 Correction : createdAt au lieu de uploadedAt
                    .expiresAt(file.getExpiresAt())
                    .expired(expired)
                    .build();

        }).collect(Collectors.toList()); // Compatible Java 8+
    }
}
