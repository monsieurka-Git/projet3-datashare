package com.datashare.backend.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre exécuté à chaque requête pour vérifier la présence
 * et la validité du JWT dans le header Authorization.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 🔹 Ignore les endpoints d'authentification
        String path = request.getServletPath();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // Vérifie si un token est présent dans le header
        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            // Vérifie si le token est valide
            if (jwtProvider.validateToken(token)) {

                Claims claims = jwtProvider.getClaims(token);

                // ✅ Récupère l'UUID utilisateur depuis le subject du token
                // (le subject est défini comme user.getId().toString() dans JwtProvider.generateToken)
                String userId = claims.getSubject();

                // ✅ Crée une authentification avec l'UUID comme principal
                // Cela permet au controller de récupérer l'ID via auth.getName()
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,      // auth.getName() = UUID de l'utilisateur
                                null,
                                null
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
