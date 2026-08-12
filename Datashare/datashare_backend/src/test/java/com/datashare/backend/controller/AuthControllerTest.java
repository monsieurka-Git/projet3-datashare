package com.datashare.backend.controller;

import com.datashare.backend.dto.AuthRequest;
import com.datashare.backend.dto.AuthResponse;
import com.datashare.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    @Test
    void register_returnsOk() {
        AuthRequest req = new AuthRequest("a@b.com", "password12");
        doNothing().when(authService).register(req);

        ResponseEntity<String> res = authController.register(req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().toLowerCase().contains("succès"));
        verify(authService).register(req);
    }

    @Test
    void login_returnsAuthResponse() {
        AuthRequest req = new AuthRequest("a@b.com", "password12");
        AuthResponse ar = new AuthResponse("jwt", "Bearer", 3600L, UUID.randomUUID(), "a@b.com");
        when(authService.login(req)).thenReturn(ar);

        ResponseEntity<AuthResponse> res = authController.login(req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("jwt", res.getBody().getToken());
    }
}
