package com.datashare.backend.dto;

import lombok.Data;

/**
 * DTO utilisé pour les requêtes register et login.
 * Contient email, password, firstname, lastname.
 */
@Data
public class AuthRequest {
    private String email;
    private String password;
}
