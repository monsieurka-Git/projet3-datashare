package com.datashare.backend.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock private JwtProvider jwtProvider;
    @Mock private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsPublicAuthPath() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        req.setServletPath("/api/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilter(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        verify(jwtProvider, never()).validateToken(anyString());
    }

    @Test
    void skipsAnonymousUpload() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/files/upload/anonymous");
        req.setServletPath("/api/files/upload/anonymous");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilter(req, res, filterChain);
        verify(filterChain).doFilter(req, res);
        verify(jwtProvider, never()).validateToken(anyString());
    }

    @Test
    void setsAuthenticationWhenTokenValid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/files");
        req.setServletPath("/api/files");
        req.addHeader("Authorization", "Bearer good.jwt.token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtProvider.validateToken("good.jwt.token")).thenReturn(true);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("11111111-1111-1111-1111-111111111111");
        when(jwtProvider.getClaims("good.jwt.token")).thenReturn(claims);

        jwtFilter.doFilter(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("11111111-1111-1111-1111-111111111111",
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void continuesWithoutAuthWhenNoHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/files");
        req.setServletPath("/api/files");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilter(req, res, filterChain);
        verify(filterChain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
