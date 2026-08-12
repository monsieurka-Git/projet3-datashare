package com.datashare.backend.JWT;

import com.datashare.backend.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        // Clé ≥ 256 bits pour HMAC-SHA
        ReflectionTestUtils.setField(jwtProvider, "secret",
                "datashare-test-secret-key-32chars-minimum!!");
        ReflectionTestUtils.setField(jwtProvider, "expiration", 3600000L);
    }

    @Test
    void generateAndValidateToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("u@test.com");

        String token = jwtProvider.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));

        Claims claims = jwtProvider.getClaims(token);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals("u@test.com", claims.get("email"));
    }

    @Test
    void validateToken_rejectsInvalid() {
        assertFalse(jwtProvider.validateToken("not.a.valid.token"));
    }

    @Test
    void getExpiration_returnsConfiguredValue() {
        assertEquals(3600000L, jwtProvider.getExpiration());
    }
}
