package com.datashare.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour la gestion des fichiers uploadés.
 * 
 * Responsabilités :
 * - Upload d'un fichier (sauvegarde disque + base de données)
 * - Recherche de fichiers par ID, token ou utilisateur
 * - Suppression d'un fichier par son propriétaire (US06)
 * 
 * Le dossier de stockage "uploads" est créé automatiquement à la racine du projet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    // 📁 Dossier de stockage des fichiers uploadés (relatif à la racine du projet)
    private final Path storagePath = Paths.get("uploads");

    /**
     * Sauvegarde une entité FileEntity en base de données uniquement.
     */
    public FileEntity save(FileEntity file) {
        return fileRepository.save(file);
    }

    /**
     * ✅ Upload d'un fichier : sauvegarde sur le disque ET en base de données.
     * 
     * Étapes :
     * 1. Création du dossier "uploads" s'il n'existe pas
     * 2. Génération d'un nom unique (UUID_nomOriginal)
     * 3. Copie du fichier sur le disque
     * 4. Création de l'entité FileEntity avec toutes les métadonnées
     * 5. Sauvegarde en base de données
     * 6. Génération d'un token unique pour le lien de téléchargement (US02)
     * 7. Association du propriétaire (US05 / US06)
     */
    public FileEntity uploadFile(MultipartFile file, UUID userId) throws IOException {
        log.debug("📤 Upload du fichier : {} (taille: {}, type: {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 📁 Crée le dossier uploads s'il n'existe pas
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.debug("📁 Dossier créé : {}", storagePath.toAbsolutePath());
        }

        // 🔀 Génère un nom unique pour éviter les collisions
        String safeOriginalName = Normalizer.normalize(file.getOriginalFilename(), Normalizer.Form.NFC);
        String storedName = UUID.randomUUID() + "_" + safeOriginalName;
        Path target = storagePath.resolve(storedName);

        // 💾 Copie le fichier sur le disque
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("💾 Fichier sauvegardé sur le disque : {}", target.toAbsolutePath());

        // 🏗 Crée et remplit l'entité FileEntity
        FileEntity entity = new FileEntity();
        entity.setFilename(storedName);
        entity.setSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setStoragePath(target.toString());
        entity.setCreatedAt(Instant.now());
        entity.setOriginalName(safeOriginalName);
        entity.setDownloadToken(UUID.randomUUID().toString());
        entity.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        // 🔐 Associe le propriétaire (US05 / US06)
        entity.setOwnerId(userId);

        // 🗃 Sauvegarde en base
        FileEntity saved = fileRepository.save(entity);
        log.debug("✅ Fichier enregistré en base avec l'ID : {}", saved.getId());

        return saved;
    }

    /** Recherche par ID */
    public Optional<FileEntity> findById(UUID id) {
        return fileRepository.findById(id);
    }

    /** Recherche par token (US02) */
    public Optional<FileEntity> findByToken(String token) {
        return fileRepository.findByDownloadToken(token);
    }

    /** Historique utilisateur (US05) */
    public List<FileEntity> findByUser(UUID userId) {
        return fileRepository.findByOwnerId(userId);
    }

    /** Suppression simple en base */
    public void delete(UUID id) {
        fileRepository.deleteById(id);
    }

    /**
     * US06 — Suppression complète d'un fichier par son propriétaire.
     */
    public void deleteFileForUser(UUID fileId, UUID userId) {
        log.debug("🗑 Suppression du fichier {} par l'utilisateur {}", fileId, userId);

        // 🔍 Recherche du fichier
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable avec l'ID : " + fileId));

        // 🔐 Vérification du propriétaire
        UUID ownerId = file.getOwnerId();
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new RuntimeException("Vous ne pouvez supprimer que vos propres fichiers");
        }

        // 🗑 Suppression physique
        try {
            Path path = Paths.get(file.getStoragePath());
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.debug("✅ Fichier physique supprimé : {}", path.toAbsolutePath());
            } else {
                log.warn("⚠️ Fichier physique introuvable sur le disque : {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier physique : " + e.getMessage());
        }

        // 🗃 Suppression en base
        fileRepository.delete(file);
        log.debug("✅ Métadonnées du fichier {} supprimées de la base", fileId);
    }
}
