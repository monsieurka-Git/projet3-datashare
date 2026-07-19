package com.datashare.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.datashare.backend.model.User;
import com.datashare.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(String email, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return userRepository.save(user);  // hash plus tard (étape 4)
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
        .filter(u -> u.getPasswordHash().equals(password));
    }
}