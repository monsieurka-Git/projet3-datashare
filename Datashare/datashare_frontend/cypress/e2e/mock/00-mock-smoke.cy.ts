/**
 * Specs MOCK uniquement — activés avec CYPRESS_MODE=mock
 */
describe('Mode mock — smoke UI', () => {
  before(function () {
    if (Cypress.env('mode') !== 'mock') this.skip();
  });

  beforeEach(() => {
    cy.mockAuthApi();
    cy.mockFilesApi();
  });

  it('login mock stocke un token', () => {
    cy.visit('/login');
    cy.get('input[name="email"]').type('user@datashare.test');
    cy.get('input[name="password"]').type('password1');
    cy.contains('button', /Connexion/i).click();
    cy.wait('@login');
  });

  it('home mock affiche rapport.pdf', () => {
    cy.visitHomeAsUser();
    cy.contains('rapport.pdf').should('be.visible');
  });
});
