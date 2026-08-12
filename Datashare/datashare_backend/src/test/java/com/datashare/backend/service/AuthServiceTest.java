package com.datashare.backend.service;

import com.datashare.backend.JWT.JwtProvider;
import com.datashare.backend.dto.AuthRequest;
import com.datashare.backend.dto.AuthResponse;
import com.datashare.backend.model.User;
import com.datashare.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires AuthService — US03 (register) / US04 (login)
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;

    @InjectMocks private AuthService authService;

    private AuthRequest request;

    @BeforeEach
    void setUp() {
        request = new AuthRequest("user@test.com", "password123");
    }

    // ---------- US03 register ----------

    @Test
    void register_createsUserWithHashedPassword() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("user@test.com", saved.getEmail());
        assertEquals("hashed-pwd", saved.getPasswordHash());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_rejectsDuplicateEmail() {
        User existing = new User();
        existing.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(userRepository, never()).save(any());
    }

    // ---------- US04 login ----------

    @Test
    void login_returnsJwtForValidCredentials() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setPasswordHash("hashed-pwd");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pwd")).thenReturn(true);
        when(jwtProvider.generateToken(user)).thenReturn("jwt-token");
        when(jwtProvider.getExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(3600000L, response.getExpiresIn());
        assertEquals(userId, response.getUserId());
        assertEquals("user@test.com", response.getEmail());
    }

    @Test
    void login_rejectsWrongPassword() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPasswordHash("hashed-pwd");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pwd")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().toLowerCase().contains("invalide")
                || ex.getMessage().toLowerCase().contains("mot de passe"));
        verify(jwtProvider, never()).generateToken(any());
    }

    @Test
    void login_rejectsUnknownEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
        verify(jwtProvider, never()).generateToken(any());
    }
}
