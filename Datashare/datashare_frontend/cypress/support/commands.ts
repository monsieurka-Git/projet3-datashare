/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /** true si mode mock */
      isMockMode(): Chainable<boolean>;
      /** URL API backend */
      apiUrl(): Chainable<string>;
      /** Inscription (ignore 4xx si déjà existant) */
      apiRegister(email: string, password?: string): Chainable<void>;
      /** Login API → stocke JWT + userId dans localStorage navigateur */
      apiLogin(email: string, password?: string): Chainable<string>;
      /** Prépare un utilisateur unique et le connecte (localStorage prêt pour cy.visit) */
      ensureUser(email?: string): Chainable<{ email: string; token: string }>;
      /** Mocks (mode mock uniquement) */
      mockAuthApi(): Chainable<void>;
      mockFilesApi(): Chainable<void>;
      visitHomeAsUser(): Chainable<void>;
    }
  }
}

const DEFAULT_PASSWORD = () => Cypress.env('testPassword') || 'password1';

Cypress.Commands.add('isMockMode', () => {
  return cy.wrap(Cypress.env('mode') === 'mock');
});

Cypress.Commands.add('apiUrl', () => {
  return cy.wrap((Cypress.env('apiUrl') as string) || 'http://localhost:8080');
});

Cypress.Commands.add('apiRegister', (email: string, password?: string) => {
  const pwd = password || DEFAULT_PASSWORD();
  cy.apiUrl().then((base) => {
    cy.request({
      method: 'POST',
      url: `${base}/api/auth/register`,
      body: { email, password: pwd },
      failOnStatusCode: false
    }).then((res) => {
      // 200/201 OK, 400/409 déjà existant → on continue
      expect([200, 201, 400, 409, 500]).to.include(res.status);
    });
  });
});

Cypress.Commands.add('apiLogin', (email: string, password?: string) => {
  const pwd = password || DEFAULT_PASSWORD();
  return cy.apiUrl().then((base) => {
    return cy
      .request({
        method: 'POST',
        url: `${base}/api/auth/login`,
        body: { email, password: pwd },
        failOnStatusCode: false
      })
      .then((res) => {
        expect(res.status, `login ${email}`).to.eq(200);
        const token = res.body.token as string;
        const userId = (res.body.userId as string) || '';
        // IMPORTANT : écrire dans le localStorage du navigateur Cypress (pas window Node)
        cy.window({ log: false }).then((win) => {
          win.localStorage.setItem('token', token);
          if (userId) win.localStorage.setItem('userId', userId);
        });
        return cy.wrap(token);
      });
  });
});

Cypress.Commands.add('ensureUser', (email?: string) => {
  const mail = email || `e2e_${Date.now()}_${Cypress._.random(1000, 9999)}@datashare.test`;
  cy.apiRegister(mail);
  return cy.apiLogin(mail).then((token) => {
    return cy.wrap({ email: mail, token: token as unknown as string });
  });
});

/* ---------- Mode MOCK (optionnel) ---------- */

const FAKE_JWT =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEifQ.e2e';
const FAKE_USER = '11111111-1111-1111-1111-111111111111';
const MOCK_FILE = {
  id: 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
  filename: 'uuid_rapport.pdf',
  originalName: 'rapport.pdf',
  size: 2048,
  contentType: 'application/pdf',
  createdAt: new Date().toISOString(),
  expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
  ownerId: FAKE_USER,
  downloadToken: 'download-token-e2e-1234',
  tags: 'test'
};

Cypress.Commands.add('mockAuthApi', () => {
  cy.intercept('POST', '**/api/auth/register', { statusCode: 200, body: 'OK' }).as('register');
  cy.intercept('POST', '**/api/auth/login', {
    statusCode: 200,
    body: { token: FAKE_JWT, type: 'Bearer', userId: FAKE_USER, email: 'user@datashare.test' }
  }).as('login');
});

Cypress.Commands.add('mockFilesApi', () => {
  cy.intercept('GET', '**/api/files/history', { statusCode: 200, body: [MOCK_FILE] }).as('history');
  cy.intercept('GET', '**/api/files', { statusCode: 200, body: [MOCK_FILE] }).as('listFiles');
  cy.intercept('GET', '**/api/files/metadata/**', {
    statusCode: 200,
    body: {
      originalName: 'rapport.pdf',
      size: 2048,
      contentType: 'application/pdf',
      passwordProtected: false
    }
  }).as('metadata');
  cy.intercept('POST', '**/api/files/download/**', {
    statusCode: 200,
    body: 'fake-pdf',
    headers: { 'content-type': 'application/pdf' }
  }).as('download');
  cy.intercept('POST', '**/api/files/upload', {
    statusCode: 200,
    body: {
      id: MOCK_FILE.id,
      downloadToken: MOCK_FILE.downloadToken,
      downloadUrl: `/download/${MOCK_FILE.downloadToken}`
    }
  }).as('upload');
  cy.intercept('POST', '**/api/files/upload/anonymous', {
    statusCode: 200,
    body: {
      id: MOCK_FILE.id,
      downloadToken: MOCK_FILE.downloadToken,
      downloadUrl: `/download/${MOCK_FILE.downloadToken}`
    }
  }).as('uploadAnonymous');
  cy.intercept('DELETE', '**/api/files/info/**', { statusCode: 204 }).as('deleteFile');
});

Cypress.Commands.add('visitHomeAsUser', () => {
  cy.visit('/home', {
    onBeforeLoad(win) {
      win.localStorage.setItem('token', FAKE_JWT);
      win.localStorage.setItem('userId', FAKE_USER);
    }
  });
});

export {};
