package com.datashare.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO pour exposer un utilisateur sans son mot de passe.
 */
@Data
public class UserDto {
    private Long id;
    private String email;
   // Champ supprimé : la liste de rôles n'existe plus.
// UserDto ne transporte que les données utilisateur essentielles (id, email, createdAt, etc.)

}
