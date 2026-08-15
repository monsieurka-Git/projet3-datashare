package com.datashare.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void securityConfig_shouldNotBeNull() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config);
    }
}
