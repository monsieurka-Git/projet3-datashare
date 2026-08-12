package com.datashare.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;


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
@Service
public class FileService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    public FileService(FileRepository fileRepository, PasswordEncoder passwordEncoder) {
        this.fileRepository = fileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 📁 Dossier de stockage des fichiers uploadés (relatif à la racine du projet)
    private Path storagePath = Paths.get("uploads");

    // 🚫 Extensions de fichiers interdites (politique de sécurité US01)
    private static final Set<String> FORBIDDEN_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "bat", "cmd", "com", "msi", "scr", "ps1", "vbs", "js", "jar", "sh", "dll", "sys"
    ));

    /**
     * Sauvegarde une entité FileEntity en base de données uniquement.
     */
    public FileEntity save(FileEntity file) {
        return fileRepository.save(file);
    }

    /**
     * ✅ Upload d'un fichier : sauvegarde sur le disque ET en base de données.
     * 
     * US01 — Règles de gestion :
     * - Taille maximale : 1 Go
     * - Fichiers interdits : .exe, .bat, .cmd, .com, .msi, .scr, .ps1, .vbs, .js, .jar, .sh, .dll, .sys
     * - Mot de passe optionnel : minimum 6 caractères
     * - Date d'expiration : entre 1 et 7 jours (défaut : 7 jours)
     * - Tags optionnels : texte libre, max 30 caractères par tag
     * 
     * @param file Fichier à uploader
     * @param userId UUID du propriétaire
     * @param expiresInDays Nombre de jours avant expiration (1-7, défaut 7)
     * @param password Mot de passe optionnel pour protéger le téléchargement (≥ 6 caractères)
     * @param tags Tags optionnels séparés par des virgules
     * @return L'entité FileEntity sauvegardée
     */
    public FileEntity uploadFile(MultipartFile file, UUID userId, Integer expiresInDays, String password, String tags) throws IOException {
        log.debug("📤 Upload du fichier : {} (taille: {}, type: {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 🚫 1. Vérification de l'extension du fichier (fichiers interdits)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        if (FORBIDDEN_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Type de fichier interdit : ." + extension);
        }

        // 📏 2. Vérification de la taille maximale (1 Go)
        if (file.getSize() > 1024L * 1024L * 1024L) {
            throw new RuntimeException("La taille maximale autorisée est de 1 Go");
        }

        // ⏰ 3. Vérification de la date d'expiration (1-7 jours, défaut 7)
        int days = (expiresInDays == null) ? 7 : expiresInDays;
        if (days < 1 || days > 7) {
            throw new RuntimeException("La date d'expiration doit être comprise entre 1 et 7 jours");
        }

        // 🔒 4. Vérification du mot de passe (minimum 6 caractères si renseigné)
        if (password != null && !password.isBlank() && password.length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        // 📁 5. Crée le dossier uploads s'il n'existe pas
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.debug("📁 Dossier créé : {}", storagePath.toAbsolutePath());
        }

        // 🔀 6. Génère un nom unique pour éviter les collisions
        String safeOriginalName = Normalizer.normalize(originalFilename, Normalizer.Form.NFC);
        String storedName = UUID.randomUUID() + "_" + safeOriginalName;
        Path target = storagePath.resolve(storedName);

        // 7. Copie le fichier sur le disque
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Fichier sauvegardé sur le disque : {}", target.toAbsolutePath());

        // 🏗 8. Crée et remplit l'entité FileEntity
        FileEntity entity = new FileEntity();
        entity.setFilename(storedName);
        entity.setSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setStoragePath(target.toString());
        entity.setCreatedAt(Instant.now());
        entity.setOriginalName(safeOriginalName);
        entity.setDownloadToken(UUID.randomUUID().toString());
        entity.setExpiresAt(Instant.now().plus(days, ChronoUnit.DAYS));

        // 🔒 9. Hash du mot de passe si renseigné (US09)
        if (password != null && !password.isBlank()) {
            entity.setDownloadPasswordHash(passwordEncoder.encode(password));
        }

        // 🏷 10. Tags optionnels (US08) — réservés aux utilisateurs connectés
        // Texte libre, max 30 car. par tag, pas de doublons
        if (tags != null && !tags.isBlank()) {
            if (userId == null) {
                throw new RuntimeException("Les tags sont réservés aux utilisateurs connectés");
            }
            entity.setTags(normalizeTags(tags));
        }

        // 🔐 11. Associe le propriétaire (null = upload anonyme US07)
        entity.setOwnerId(userId);

        // 🗃 12. Sauvegarde en base
        FileEntity saved = fileRepository.save(entity);
        log.debug("✅ Fichier enregistré en base avec l'ID : {}", saved.getId());

        return saved;
    }

    /**
     * ✅ Méthode de compatibilité : upload avec les valeurs par défaut (7 jours, sans mot de passe, sans tags).
     */
    public FileEntity uploadFile(MultipartFile file, UUID userId) throws IOException {
        return uploadFile(file, userId, 7, null, null);
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

    /**
     * US08 — Normalise la liste de tags :
     * - split par virgule
     * - trim, ignore vides
     * - max 30 caractères par tag
     * - pas de doublons (insensible à la casse)
     */
    public String normalizeTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return null;
        }
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        for (String part : rawTags.split(",")) {
            String tag = part.trim();
            if (tag.isEmpty()) continue;
            if (tag.length() > 30) {
                throw new RuntimeException("Chaque tag ne doit pas dépasser 30 caractères : " + tag);
            }
            // Anti-doublon (casse ignorée)
            boolean already = unique.stream().anyMatch(u -> u.equalsIgnoreCase(tag));
            if (!already) {
                unique.add(tag);
            }
        }
        if (unique.isEmpty()) return null;
        return String.join(",", unique);
    }

    /**
     * US08 — Met à jour les tags d'un fichier appartenant à l'utilisateur.
     */
    public FileEntity updateTags(UUID fileId, UUID userId, String tags) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable avec l'ID : " + fileId));
        if (file.getOwnerId() == null || !file.getOwnerId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres fichiers");
        }
        file.setTags(normalizeTags(tags));
        return fileRepository.save(file);
    }

    /**
     * US10 — Purge des fichiers expirés (physique + base).
     * @return nombre de fichiers supprimés
     */
    public int purgeExpiredFiles() {
        java.time.Instant now = java.time.Instant.now();
        java.util.List<FileEntity> expired = fileRepository.findByExpiresAtBefore(now);
        int count = 0;
        for (FileEntity file : expired) {
            try {
                Path path = Paths.get(file.getStoragePath());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                log.warn("Impossible de supprimer le fichier physique {}: {}", file.getStoragePath(), e.getMessage());
            }
            fileRepository.delete(file);
            count++;
            log.info("US10 — Fichier expiré purgé : {} ({})", file.getOriginalName(), file.getId());
        }
        return count;
    }
}
