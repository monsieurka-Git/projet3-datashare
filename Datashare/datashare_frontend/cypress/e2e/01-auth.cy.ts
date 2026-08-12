/**
 * Scénario E2E 1 — Création de compte et connexion (US03, US04)
 * TESTING.md § 2.2 Scénario 1
 */
describe('US03 / US04 — Inscription et connexion', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
    cy.mockAuthApi();
  });

  it('affiche la page d\'accueil (welcome)', () => {
    cy.visit('/welcome');
    cy.contains('Tu veux partager un fichier').should('be.visible');
    cy.get('.ds-cloud-btn, [aria-label="Partager un fichier"]').should('exist');
  });

  it('permet de créer un compte puis de se connecter', () => {
    // 1. Accueil
    cy.visit('/welcome');

    // 2. Aller vers login via header ou navigation directe
    cy.visit('/login');
    cy.contains('h1', 'Connexion').should('be.visible');

    // 3. Lien vers inscription
    cy.contains('Créer un compte').click();
    cy.url().should('include', '/register');
    cy.contains('h1', 'Créer un compte').should('be.visible');

    // 4. Saisie email + mot de passe ≥ 8 caractères
    const email = `e2e_${Date.now()}@datashare.test`;
    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type('password1');
    cy.get('input[name="confirmPassword"]').clear().type('password1');

    // 5. Soumission
    cy.contains('button', 'Créer mon compte').click();
    cy.wait('@register');

    // Succès ou redirection login
    cy.get('body').then(($body) => {
      if ($body.find('.ds-success').length) {
        cy.get('.ds-success').should('be.visible');
      }
    });

    // 6. Connexion
    cy.visit('/login');
    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type('password1');
    cy.contains('button', 'Connexion').click();
    cy.wait('@login');

    // 7. JWT stocké + accès espace personnel
    cy.window().then((win) => {
      const token = win.localStorage.getItem('token');
      expect(token).to.be.a('string').and.not.be.empty;
    });

    // Redirection attendue vers /home (selon login.ts)
    cy.url().should('match', /\/(home|welcome)/);
  });

  it('rejette un mot de passe trop court à l\'inscription (validation UI)', () => {
    cy.visit('/register');
    cy.get('input[name="email"]').type('short@test.com');
    cy.get('input[name="password"]').type('short');
    cy.get('input[name="confirmPassword"]').type('short');
    cy.contains('button', 'Créer mon compte').click();
    // HTML5 minlength ou message d'erreur applicatif
    cy.get('input[name="password"]:invalid, .ds-error').should('exist');
  });
});
