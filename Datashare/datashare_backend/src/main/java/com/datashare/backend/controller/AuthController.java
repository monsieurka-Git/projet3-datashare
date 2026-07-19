package com.datashare.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datashare.backend.dto.AuthRequest;
import com.datashare.backend.dto.UserDto;
import com.datashare.backend.model.User;
import com.datashare.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public UserDto register(@RequestBody AuthRequest request) {
        User user = authService.register(request.email(), request.password());
        return new UserDto(user.getId(), user.getEmail());
    }

   @PostMapping("/login")
public UserDto login(@RequestBody AuthRequest request) {
    return authService.login(request.email(), request.password())
            .map(u -> new UserDto(u.getId(), u.getEmail()))
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
}

}