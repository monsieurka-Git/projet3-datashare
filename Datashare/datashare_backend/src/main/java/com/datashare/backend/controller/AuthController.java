package com.datashare.backend.controller;

import com.datashare.backend.dto.AuthRequest;
import com.datashare.backend.dto.AuthResponse;
import com.datashare.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification.
 * Contient les endpoints register (US03) et login (US04).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * US03 — Inscription utilisateur.
     * Reçoit email, password.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Utilisateur créé avec succès");
    }

    /**
     * US04 — Connexion utilisateur + génération du JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
