import { defineConfig } from 'cypress';


/**
 * DataShare E2E
 *
 * Modes (env) :
 *   CYPRESS_MODE=real  → API Spring + PostgreSQL (défaut recommandé soutenance)
 *   CYPRESS_MODE=mock  → cy.intercept, backend non requis
 *
 * Prérequis mode real :
 *   - Spring Boot :8080
 *   - Angular :4200
 *   - PostgreSQL démarré
 */
export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 15000,
    requestTimeout: 15000,
    viewportWidth: 1280,
    viewportHeight: 800,
    env: {
      // real | mock
      mode: 'real',
      apiUrl: 'http://localhost:8080',
      // Mot de passe ≥ 8 caractères (validation UI register)
      testPassword: 'password1'
    },
    setupNodeEvents(on, config) {
      // Permet : CYPRESS_MODE=mock npm run e2e:run
      const mode = process.env.CYPRESS_MODE || config.env.mode || 'real';
      config.env.mode = mode;
      if (process.env.CYPRESS_API_URL) {
        config.env.apiUrl = process.env.CYPRESS_API_URL;
      }
      return config;
    }
  }
});
