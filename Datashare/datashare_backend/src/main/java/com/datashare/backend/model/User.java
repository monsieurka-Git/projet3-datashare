package com.datashare.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité représentant un utilisateur dans la base de données.
 * Conformément aux specs DataShare : email + mot de passe hashé + date de création.
 * Aucun rôle n'est utilisé dans ce projet.
 */
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    /**
     * Identifiant unique de l'utilisateur.
     *
     * ⚠ IMPORTANT :
     * Ta table PostgreSQL utilise un UUID comme type pour la colonne "id".
     * Donc on doit utiliser GenerationType.UUID (Hibernate 6 / Spring Boot 3).
     *
     * GenerationType.IDENTITY NE FONCTIONNE PAS avec un UUID.
     * C'était la cause de l'erreur :
     * "une valeur NULL viole la contrainte NOT NULL de la colonne id"
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email de l'utilisateur.
     * - Doit être unique (contrainte en base + annotation unique = true)
     * - Ne peut pas être null (nullable = false)
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Mot de passe hashé (BCrypt).
     * - Jamais stocké en clair.
     * - Toujours généré via passwordEncoder.encode(...)
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Date de création du compte.
     * - Optionnelle selon les specs.
     * - Peut être remplie automatiquement dans AuthService.register().
     */
    /* nullable = false → jamais de valeur NULL  
     *updatable = false → la date ne peut pas être modifiée après création*/
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
