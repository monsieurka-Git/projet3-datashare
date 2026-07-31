package com.datashare.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration CORS pour permettre les requêtes depuis Postman et le frontend.
 * 
 * ⚠️ En développement, on autorise toutes les origines.
 * En production, restreindre aux origines autorisées.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOrigin("*"); // Autorise toutes les origines (Postman, frontend, etc.)
        config.addAllowedHeader("*"); // Autorise tous les headers
        config.addAllowedMethod("*"); // Autorise toutes les méthodes HTTP (GET, POST, PUT, DELETE, etc.)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}