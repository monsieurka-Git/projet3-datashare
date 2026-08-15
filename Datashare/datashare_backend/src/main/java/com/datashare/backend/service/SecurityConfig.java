package com.datashare.backend.service;

import com.datashare.backend.JWT.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

   @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        // 🔥 CORS doit être activé AVANT toute règle de sécurité
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        .authorizeHttpRequests(auth -> auth

                // 🔥 Autoriser explicitement le preflight OPTIONS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Endpoints publics
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/files/metadata/**",
                        "/api/files/download/**",
                        "/api/files/upload/anonymous"
                ).permitAll()

                // Upload authentifié nécessite un JWT (US01)
                .requestMatchers(HttpMethod.POST, "/api/files/upload").authenticated()

                .anyRequest().authenticated()
        );

    // 🔥 IMPORTANT : placer le filtre JWT APRÈS CORS
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // Origines autorisées (dev Angular + éventuellement d'autres)
    config.setAllowedOriginPatterns(java.util.List.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200"
    ));
    // Autoriser tous les headers (plus robuste pour multipart + JWT + preflight)
    config.setAllowedHeaders(java.util.List.of("*"));
    // Méthodes autorisées
    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    // Nécessaire car le frontend envoie le cookie/Authorization
    config.setAllowCredentials(true);
    // Exposer les headers utiles au frontend si besoin
    config.setExposedHeaders(java.util.List.of("Authorization", "Content-Disposition"));
    // Cache du preflight (optionnel, réduit les OPTIONS)
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}




    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
