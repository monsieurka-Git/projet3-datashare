package com.datashare.backend.dto;

public record AuthRequest(
    String email,
    String password
) {}