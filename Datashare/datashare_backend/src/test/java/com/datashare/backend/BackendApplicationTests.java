package com.datashare.backend;

import org.junit.jupiter.api.Test;

/**
 * Smoke test minimal — les tests métier sont dans service/*.
 * Évite de charger le contexte Spring (DB requise).
 */
class BackendApplicationTests {

    @Test
    void contextPlaceholder() {
        // Les tests unitaires métier (AuthService, FileService, etc.)
        // n'ont pas besoin du contexte Spring complet.
        org.junit.jupiter.api.Assertions.assertTrue(true);
    }
}
