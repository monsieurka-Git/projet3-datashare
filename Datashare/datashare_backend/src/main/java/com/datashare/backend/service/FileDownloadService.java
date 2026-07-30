package com.datashare.backend.service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Service gérant le téléchargement via lien unique (US02).
 * Règles respectées :
 * - Token unique non prédictible
 * - Mot de passe optionnel
 * - Erreur explicite si lien expiré ou invalide
 * - Métadonnées visibles avant téléchargement
 */
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    // Dossier où les fichiers sont stockés
    private final Path storagePath = Paths.get("uploads");

    /**
     * Récupère un fichier via son token.
     * Vérifie :
     * - existence du token
     * - expiration
     * - mot de passe si protégé
     */
    public FileEntity getFileForDownload(String token, String password) {

        // Recherche optimisée via JPA
        FileEntity file = fileRepository.findByDownloadToken(token)
                .orElseThrow(() -> new RuntimeException("Lien invalide"));

        // Vérification expiration
        if (file.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Lien expiré");
        }

        // Vérification mot de passe si protégé
        if (file.getDownloadPasswordHash() != null) {

            // Mot de passe obligatoire
            if (password == null || password.isBlank()) {
                throw new RuntimeException("Mot de passe requis");
            }

            // Vérification du mot de passe
            if (!passwordEncoder.matches(password, file.getDownloadPasswordHash())) {
                throw new RuntimeException("Mot de passe incorrect");
            }
        }

        return file;
    }

    /**
     * Charge le fichier physique depuis le disque.
     */
    public byte[] loadFileBytes(String filename) throws Exception {
        Path filePath = storagePath.resolve(filename);
        return Files.readAllBytes(filePath);
    }
}
