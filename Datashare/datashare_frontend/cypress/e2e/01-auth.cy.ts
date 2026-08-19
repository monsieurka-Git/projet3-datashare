/**
 * US03 / US04 — Inscription & connexion RÉELLES (PostgreSQL)
 */
import { LoginPage } from '../pages/LoginPage';

describe('US03 / US04 — Auth réelle', () => {
  const loginPage = new LoginPage();
  const password = () => (Cypress.env('testPassword') as string) || 'password1';

  before(function () {
    if (Cypress.env('mode') === 'mock') {
      this.skip();
    }
  });

  it('inscrit un utilisateur unique puis se connecte via le formulaire', () => {
    const email = `auth_${Date.now()}@datashare.test`;

    // 1. Register API → écrit en base
    cy.apiRegister(email, password());

    // 2. Login formulaire UI → JWT en localStorage
    loginPage.visit().loginAs(email, password());

    cy.window().its('localStorage').invoke('getItem', 'token').should('be.a', 'string').and('not.be.empty');
    cy.url().should('match', /\/(home|welcome)/);
  });

  it('refuse des identifiants incorrects', () => {
    loginPage.visit().loginAs('nobody_e2e@datashare.test', 'wrongpass1');
    cy.contains(/incorrects|invalide|erreur|réessayer/i, { timeout: 10000 }).should('be.visible');
  });
});
