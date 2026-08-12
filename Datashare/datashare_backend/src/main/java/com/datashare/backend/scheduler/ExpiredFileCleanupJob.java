package com.datashare.backend.scheduler;

import com.datashare.backend.service.FileService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * US10 — Tâche planifiée de purge des fichiers expirés.
 * S'exécute tous les jours à 03:00 (heure du serveur).
 * Supprime le fichier physique et les métadonnées en base.
 */
@Component
public class ExpiredFileCleanupJob {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpiredFileCleanupJob.class);

    private final FileService fileService;

    public ExpiredFileCleanupJob(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Cron : seconde minute heure jour mois jour-semaine
     * "0 0 3 * * *" = tous les jours à 03:00:00
     */
    /*@Scheduled(cron = "0 0 3 * * *") */
    public void purgeExpiredFiles() {
        log.info("US10 — Démarrage de la purge des fichiers expirés…");
        try {
            int deleted = fileService.purgeExpiredFiles();
            log.info("US10 — Purge terminée : {} fichier(s) supprimé(s)", deleted);
        } catch (Exception e) {
            log.error("US10 — Erreur pendant la purge des fichiers expirés", e);
        }
    }
}
