package com.datashare.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité JPA représentant un fichier uploadé sur la plateforme DataShare.
 * 
 * Évolution des champs pour US02 (téléchargement via lien) :
 * - originalName : nom d'origine du fichier (affiché avant téléchargement)
 * - downloadToken : token unique non prédictible servant de lien de téléchargement
 * - downloadPasswordHash : hash BCrypt du mot de passe (si protégé)
 * - expiresAt : date d'expiration du lien (Instant pour précision)
 */
@Entity
@Table(name = "files")
@Data
public class FileEntity {

    @Id
    @GeneratedValue
    private UUID id;

    /** Nom du fichier stocké sur le serveur (UUID_nomOriginal) */
    @Column(nullable = false)
    private String filename;

    /** Nom original du fichier (avant upload) — utilisé pour l'affichage */
    private String originalName;

    /** Taille du fichier en octets */
    private Long size;

    /** Type MIME du fichier (ex: application/pdf) */
    private String contentType;

    /** Chemin absolu de stockage sur le disque */
    @Column(nullable = false)
    private String storagePath;

    /** Token unique non prédictible pour le téléchargement via lien (US02) */
    @Column(unique = true)
    private String downloadToken;

    /** Hash BCrypt du mot de passe protégeant le téléchargement (nullable si public) */
    private String downloadPasswordHash;

    /** Date d'expiration du lien de téléchargement (Instant pour compatibilité avec les comparaisons) */
    private Instant expiresAt;

    /** Date de création du fichier sur le serveur */
    private Instant createdAt = Instant.now();

    /** UUID du propriétaire du fichier */
    private UUID ownerId;

    /** Tags optionnels pour catégoriser le fichier */
    private String tags;
}