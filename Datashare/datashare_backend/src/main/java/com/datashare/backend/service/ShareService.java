package com.datashare.backend.service;

import com.datashare.backend.dto.ShareRequest;
import com.datashare.backend.dto.ShareResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service de partage de fichier (US partage).
 *
 * Vérifie que le fichier appartient à l'utilisateur connecté,
 * construit le lien de téléchargement et "notifie" le destinataire.
 *
 * Note : l'envoi d'e-mail réel n'est pas configuré (pas de SMTP dans le projet).
 * On log le partage et on renvoie le lien pour que le frontend puisse l'afficher.
 */
@Service
public class ShareService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShareService.class);

    private final FileRepository fileRepository;

    public ShareService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public ShareResponse share(ShareRequest request, UUID ownerId) {
        if (request == null || request.fileId() == null || request.fileId().isBlank()) {
            throw new RuntimeException("Identifiant du fichier manquant");
        }
        if (request.recipientEmail() == null || request.recipientEmail().isBlank()) {
            throw new RuntimeException("Email du destinataire requis");
        }
        if (!request.recipientEmail().contains("@")) {
            throw new RuntimeException("Email du destinataire invalide");
        }

        UUID fileId;
        try {
            fileId = UUID.fromString(request.fileId());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Identifiant du fichier invalide");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        if (file.getOwnerId() == null || !file.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Vous ne pouvez partager que vos propres fichiers");
        }

        if (file.getExpiresAt() != null && file.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Ce fichier a expiré, partage impossible");
        }

        String token = file.getDownloadToken();
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Ce fichier n'a pas de lien de téléchargement");
        }

        // Lien relatif vers la page Angular /download/:token
        String downloadUrl = "/download/" + token;

        // Simulation d'envoi d'e-mail (à remplacer par JavaMail/SMTP en production)
        log.info("📧 PARTAGE — fichier '{}' (id={}) → {} | lien={}",
                file.getOriginalName(),
                file.getId(),
                request.recipientEmail(),
                downloadUrl);

        String message = String.format(
                "Fichier « %s » partagé avec %s. Le destinataire peut utiliser le lien de téléchargement.",
                file.getOriginalName(),
                request.recipientEmail()
        );

        return new ShareResponse(
                message,
                request.recipientEmail(),
                downloadUrl,
                token,
                file.getOriginalName()
        );
    }
}
