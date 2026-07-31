package com.datashare.backend.service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

/**
 * Service gérant le téléchargement via lien unique (US02).
 * 
 * Règles respectées :
 * - Token unique non prédictible
 * - Mot de passe optionnel pour protéger le téléchargement
 * - Erreur explicite si lien expiré ou invalide
 * - Métadonnées visibles SANS mot de passe (avant téléchargement)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    // 📁 Dossier de stockage des fichiers uploadés (relatif à la racine du projet)
    private final Path storagePath = Paths.get("uploads");

    /**
     * Récupère les métadonnées d'un fichier via son token de téléchargement ou son ID.
     * ⚠️ Contrairement à getFileForDownload(), cette méthode NE vérifie PAS le mot de passe.
     * Cela permet d'afficher les métadonnées (nom, taille, type, etc.) avant que
     * l'utilisateur ne saisisse le mot de passe pour le téléchargement effectif.
     *
     * @param token Token unique de téléchargement (ou UUID du fichier)
     * @return FileEntity si le token/ID est valide et non expiré
     * @throws RuntimeException si le token/ID est invalide ou expiré
     */
    public FileEntity getFileMetadata(String token) {

        // 🔍 Recherche du fichier par son token unique
        FileEntity file = fileRepository.findByDownloadToken(token).orElse(null);

        // Si le token n'est pas trouvé, on essaie de le chercher par ID (UUID)
        // (au cas où l'utilisateur a confondu le downloadToken avec l'ID du fichier)
        if (file == null) {
            try {
                UUID fileId = UUID.fromString(token);
                file = fileRepository.findById(fileId).orElse(null);
            } catch (IllegalArgumentException e) {
                // Le token n'est ni un UUID valide, ni un downloadToken existant
                log.debug("Token invalide : {}", token);
            }
        }

        if (file == null) {
            throw new RuntimeException("Lien invalide ou introuvable");
        }

        // ⏰ Vérification de l'expiration du lien
        if (file.getExpiresAt() != null && file.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Lien expiré");
        }

        return file;
    }

    /**
     * Récupère un fichier via son token ou son ID pour téléchargement.
     * Vérifie :
     * - Existence du token/ID
     * - Expiration du lien
     * - Mot de passe si le fichier est protégé
     *
     * @param token    Token unique de téléchargement (ou UUID du fichier)
     * @param password Mot de passe (nullable si le fichier n'est pas protégé)
     * @return FileEntity si toutes les vérifications passent
     * @throws RuntimeException si une vérification échoue
     */
    public FileEntity getFileForDownload(String token, String password) {

        // 🔍 Recherche du fichier par son token unique
        FileEntity file = fileRepository.findByDownloadToken(token).orElse(null);

        // Si le token n'est pas trouvé, on essaie de le chercher par ID (UUID)
        // (au cas où l'utilisateur a confondu le downloadToken avec l'ID du fichier)
        if (file == null) {
            try {
                UUID fileId = UUID.fromString(token);
                file = fileRepository.findById(fileId).orElse(null);
            } catch (IllegalArgumentException e) {
                // Le token n'est ni un UUID valide, ni un downloadToken existant
                log.debug("Token invalide : {}", token);
            }
        }

        if (file == null) {
            throw new RuntimeException("Lien invalide ou introuvable");
        }

        // ⏰ Vérification de l'expiration du lien
        if (file.getExpiresAt() != null && file.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Lien expiré");
        }

        // 🔐 Vérification du mot de passe si le fichier est protégé
        if (file.getDownloadPasswordHash() != null) {

            // ❌ Le mot de passe est obligatoire pour les fichiers protégés
            if (password == null || password.isBlank()) {
                throw new RuntimeException("Mot de passe requis pour télécharger ce fichier");
            }

            // ❌ Vérification du hash BCrypt
            if (!passwordEncoder.matches(password, file.getDownloadPasswordHash())) {
                throw new RuntimeException("Mot de passe incorrect");
            }
        }

        return file;
    }

    /**
     * Charge le fichier physique depuis le disque.
     * Le fichier est recherché dans le dossier "uploads" avec son nom stocké.
     *
     * @param filename Nom du fichier stocké sur le serveur (UUID_nomOriginal)
     * @return Contenu du fichier en bytes
     * @throws Exception si le fichier est introuvable ou inaccessible
     */
    public byte[] loadFileBytes(String filename) throws Exception {
        Path filePath = storagePath.resolve(filename);
        log.debug("📂 Chargement du fichier physique : {}", filePath.toAbsolutePath());
        return Files.readAllBytes(filePath);
    }
}
