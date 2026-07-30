package com.datashare.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    // ✅ Dossier de stockage des fichiers uploadés (relatif à la racine du projet)
    private final Path storagePath = Paths.get("uploads");

    /**
     * Sauvegarde un fichier dans la base de données uniquement.
     */
    public FileEntity save(FileEntity file) {
        return fileRepository.save(file);
    }

    /**
     * ✅ Upload d'un fichier : sauvegarde sur le disque ET en base de données.
     * 
     * @param file   le fichier multipart à uploader
     * @param userId l'UUID de l'utilisateur propriétaire
     * @return l'entité FileEntity sauvegardée
     * @throws IOException en cas d'erreur d'écriture sur le disque
     */
    public FileEntity uploadFile(MultipartFile file, UUID userId) throws IOException {
        log.debug("📤 Upload du fichier : {} (taille: {}, type: {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // Crée le dossier uploads s'il n'existe pas
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.debug("📁 Dossier créé : {}", storagePath.toAbsolutePath());
        }

        // Génère un nom unique pour éviter les collisions (UUID_nomOriginal)
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = storagePath.resolve(storedName);

        // Copie le fichier sur le disque (remplace si existant)
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("💾 Fichier sauvegardé sur le disque : {}", target.toAbsolutePath());

        // Crée et remplit l'entité FileEntity avec les données du fichier
        FileEntity entity = new FileEntity();
        entity.setFilename(storedName);                          // Nom unique sur le serveur
        entity.setSize(file.getSize());                          // Taille en octets
        entity.setContentType(file.getContentType());            // Type MIME
        entity.setStoragePath(target.toString());               // Chemin absolu sur le disque
        entity.setCreatedAt(Instant.now());                     // Date d'upload
        entity.setOwnerId(userId);                               // Propriétaire du fichier

        // Sauvegarde en base de données
        FileEntity saved = fileRepository.save(entity);
        log.debug("✅ Fichier enregistré en base avec l'ID : {}", saved.getId());

        return saved;
    }

    public Optional<FileEntity> findById(UUID id) {
        return fileRepository.findById(id);
    }

    public Optional<FileEntity> findByLink(String link) {
        return fileRepository.findByDownloadLink(link);
    }

    public List<FileEntity> findByUser(UUID userId) {
        return fileRepository.findByOwnerId(userId);
    }

    public void delete(UUID id) {
        fileRepository.deleteById(id);
    }
}
