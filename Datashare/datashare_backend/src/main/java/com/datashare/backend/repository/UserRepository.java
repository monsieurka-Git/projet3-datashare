package com.datashare.backend.repository;

import com.datashare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour accéder aux utilisateurs en base.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par email.
     */
    Optional<User> findByEmail(String email);
}
