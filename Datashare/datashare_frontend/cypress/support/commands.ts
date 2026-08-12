/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /** Seed localStorage avec un faux JWT (dans le contexte de l'app) */
      loginAs(email?: string, userId?: string): Chainable<void>;
      /** Intercepte les appels API auth */
      mockAuthApi(): Chainable<void>;
      /** Intercepte les appels API fichiers */
      mockFilesApi(): Chainable<void>;
      /** Visite /home déjà authentifié + mocks fichiers */
      visitHomeAsUser(): Chainable<void>;
      /** Réinitialise l'état partagé des fichiers mockés */
      resetMockFiles(): Chainable<void>;
    }
  }
}

const FAKE_JWT =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLCJlbWFpbCI6InVzZXJAZGF0YXNoYXJlLnRlc3QifQ.sig';
const FAKE_USER_ID = '11111111-1111-1111-1111-111111111111';

Cypress.Commands.add('loginAs', (email = 'user@datashare.test', userId = FAKE_USER_ID) => {
  // Important : écrire dans le localStorage de l'origine de l'app
  cy.window({ log: false }).then((win) => {
    win.localStorage.setItem('token', FAKE_JWT);
    win.localStorage.setItem('userId', userId);
  });
});

Cypress.Commands.add('mockAuthApi', () => {
  cy.intercept('POST', '**/api/auth/register', {
    statusCode: 200,
    body: 'Utilisateur créé avec succès'
  }).as('register');

  cy.intercept('POST', '**/api/auth/login', (req) => {
    const body = req.body || {};
    // Simule un échec si mot de passe = "wrong" ou email contient "bad"
    if (
      body.password === 'wrong' ||
      body.password === 'badpassword' ||
      (typeof body.email === 'string' && body.email.includes('bad@'))
    ) {
      req.reply({
        statusCode: 401,
        body: { error: 'Email ou mot de passe invalide' }
      });
      return;
    }
    req.reply({
      statusCode: 200,
      body: {
        token: FAKE_JWT,
        type: 'Bearer',
        expiresIn: 3600000,
        expiresAt: Date.now() + 3600000,
        userId: FAKE_USER_ID,
        email: body.email || 'user@datashare.test'
      }
    });
  }).as('login');
});

Cypress.Commands.add('mockFilesApi', () => {
  const fileId = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';
  const token = 'download-token-e2e-1234';

  // État partagé pour la liste des fichiers (permet la suppression)
  let files = [
    {
      id: fileId,
      filename: 'uuid_rapport.pdf',
      originalName: 'rapport.pdf',
      size: 2048,
      contentType: 'application/pdf',
      createdAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
      ownerId: FAKE_USER_ID,
      downloadToken: token,
      tags: 'travail,urgent'
    }
  ];

  // GET liste — retourne l'état courant
  cy.intercept(
    { method: 'GET', url: /\/api\/files\/?(\?.*)?$/ },
    (req) => {
      req.reply({ statusCode: 200, body: [...files] });
    }
  ).as('listFiles');

  // DELETE suppression — met à jour l'état
  cy.intercept('DELETE', '**/api/files/info/**', (req) => {
    const match = req.url.match(/\/api\/files\/info\/([^/?#]+)/);
    const idToDelete = match ? match[1] : null;
    if (idToDelete) {
      files = files.filter(f => f.id !== idToDelete);
    }
    req.reply({ statusCode: 204 });
  }).as('deleteFile');

  cy.intercept('POST', '**/api/files/upload/anonymous', {
    statusCode: 200,
    body: {
      id: fileId,
      filename: 'uuid_anon.txt',
      originalName: 'anon.txt',
      downloadToken: token,
      downloadUrl: `/download/${token}`
    }
  }).as('uploadAnonymous');

  cy.intercept('POST', '**/api/files/upload', {
    statusCode: 200,
    body: {
      id: fileId,
      filename: 'uuid_rapport.pdf',
      originalName: 'rapport.pdf',
      downloadToken: token,
      downloadUrl: `/download/${token}`
    }
  }).as('upload');

  cy.intercept('GET', '**/api/files/metadata/**', {
    statusCode: 200,
    body: {
      originalName: 'rapport.pdf',
      size: 2048,
      contentType: 'application/pdf',
      expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
      passwordProtected: false
    }
  }).as('metadata');

  cy.intercept('POST', '**/api/files/download/**', {
    statusCode: 200,
    headers: {
      'content-type': 'application/pdf',
      'content-disposition': 'attachment; filename="rapport.pdf"'
    },
    body: 'PDF-MOCK-CONTENT'
  }).as('download');

});

Cypress.Commands.add('visitHomeAsUser', () => {
  cy.mockAuthApi();
  cy.mockFilesApi();
  cy.visit('/home', {
    onBeforeLoad(win) {
      win.localStorage.setItem('token', FAKE_JWT);
      win.localStorage.setItem('userId', FAKE_USER_ID);
    }
  });
  cy.wait('@listFiles');
});

// Réinitialise l'état partagé des fichiers mockés
Cypress.Commands.add('resetMockFiles', () => {
  cy.mockFilesApi();
});

export {};
