package com.datashare.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.datashare.backend.JWT.JwtProvider;
import com.datashare.backend.dto.AuthRequest;
import com.datashare.backend.dto.AuthResponse;
import com.datashare.backend.model.User;
import com.datashare.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service gérant l'inscription (US03) et la connexion (US04).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * US03 — Création de compte.
     * Vérifie l'unicité de l'email, hash le mot de passe, crée l'utilisateur.
     */
    public void register(AuthRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Suppression de toute référence au rôle.
        // L'utilisateur est créé sans rôle, uniquement avec email et mot de passe hashé.



        userRepository.save(user);
    }

    /**
     * US04 — Connexion utilisateur + génération du JWT.
     */
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe invalide"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Email ou mot de passe invalide");
        }

        String token = jwtProvider.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtProvider.getExpiration(),
                user.getId(),
                user.getEmail()
                
        );
    }
}
