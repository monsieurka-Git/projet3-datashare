package com.datashare.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Réponse renvoyée après un login réussi.
 * Contient le JWT et les informations minimales de l'utilisateur.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;        // JWT
    private String type;         // "Bearer"
    private long expiresIn;      // durée de validité en ms
    private UUID userId;         // id utilisateur
    private String email;        // email utilisateur
    // Champ supprimé : le rôle n'est plus utilisé dans DataShare.
// AuthResponse ne contient plus que les infos nécessaires à la connexion (token, email, etc.)


}
