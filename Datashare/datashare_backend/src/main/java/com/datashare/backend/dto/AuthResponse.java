package com.datashare.backend.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String type;
    private long expiresIn;
    private UUID userId;
    private String email;

    public AuthResponse() {}

    public AuthResponse(String token, String type, long expiresIn, UUID userId, String email) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
