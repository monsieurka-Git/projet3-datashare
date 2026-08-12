import { defineConfig } from 'cypress';

export default defineConfig({
  // Désactive le warning Cypress 15 sur Cypress.env()
  // @ts-expect-error option runtime Cypress 15
  // allowCypressEnv n'est pas dans les types TS encore
  e2e: {
    baseUrl: 'http://localhost:4200',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    viewportWidth: 1280,
    viewportHeight: 800,
    env: {
      coverage: true,
      apiUrl: 'http://localhost:8080/api'
    },
    setupNodeEvents(on, config) {
      // Couverture E2E (Istanbul / nyc)
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      require('@cypress/code-coverage/task')(on, config);
      return config;
    }
  }
});
