package com.datashare.backend.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datashare.backend.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    /**
     * Génère une clé de signature HMAC à partir du secret.
     * ✅ Utilise la nouvelle API jjwt 0.11.x (non dépréciée).
     * Convertit le secret en bytes UTF-8 et crée une clé HMAC-SHA.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Génère un JWT contenant l'UUID et l'email de l'utilisateur.
     * ✅ Utilise la nouvelle API jjwt 0.11.x (signWith avec SecretKey).
     * 
     * @param user l'utilisateur authentifié
     * @return le token JWT signé
     */
    public String generateToken(User user) {
        return Jwts.builder()
                // Le subject = UUID de l'utilisateur (permet de l'extraire facilement dans le filtre)
                .setSubject(user.getId().toString())
                // Email en claim supplémentaire
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                // ✅ Nouvelle API : signWith(SecretKey) au lieu de signWith(SignatureAlgorithm, String)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Vérifie si le token est valide (non expiré, signature correcte).
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Récupère les claims (données) du token.
     * ✅ Utilise la nouvelle API jjwt 0.11.x (parserBuilder au lieu de parser).
     * 
     * @param token le token JWT
     * @return les claims contenus dans le token
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retourne la durée d'expiration du token en millisecondes.
     */
    public long getExpiration() {
        return expiration;
    }
}
