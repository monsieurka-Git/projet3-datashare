/**
 * Login Page — tests ciblés formulaire
 */
describe('Login Page', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
    cy.visit('/login');
  });

  it('affiche le formulaire de connexion', () => {
    cy.get('input[type="email"]').should('exist');
    cy.get('input[type="password"]').should('exist');
    cy.get('button[type="submit"]').should('exist');
  });

  it('refuse une connexion avec identifiants invalides', () => {
    // Mock login invalide
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 401,
      body: { error: 'Identifiants incorrects. Veuillez réessayer.' }
    }).as('loginInvalid');

    cy.get('input[type="email"]').type('wrong@test.com');
    cy.get('input[type="password"]').type('badpass');
    cy.get('button[type="submit"]').click();

    cy.wait('@loginInvalid');

    // ✅ Correction : correspondance exacte du message affiché
    cy.get('p.ds-error', { timeout: 10000 })
      .should('exist')
      .and('contain.text', 'Identifiants incorrects. Veuillez réessayer.');
  });

  it('accepte une connexion valide', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        token: 'fake-jwt-token-for-e2e',
        type: 'Bearer',
        expiresIn: 3600000,
        expiresAt: Date.now() + 3600000,
        userId: '11111111-1111-1111-1111-111111111111',
        email: 'user@datashare.test'
      }
    }).as('login');

    cy.get('input[type="email"]').type('user@datashare.test');
    cy.get('input[type="password"]').type('123456');
    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.url().should('include', '/home');
  });
});
