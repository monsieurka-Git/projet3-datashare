package com.datashare.backend.dto;

import lombok.Data;

/**
 * DTO minimal si tu veux une réponse simplifiée pour le login.
 * Non utilisé si AuthResponse suffit.
 */
@Data
public class LoginResponse {
    private String token;
}
