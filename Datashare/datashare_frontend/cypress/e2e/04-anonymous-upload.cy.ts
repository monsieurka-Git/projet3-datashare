/**
 * US07 — Upload anonyme
 */
describe('US07 — Upload anonyme depuis la page welcome', () => {
  beforeEach(() => {
    cy.clearAllLocalStorage();
    cy.mockFilesApi();
  });

  it('redirige le bouton cloud vers /upload sans être connecté', () => {
    cy.visit('/welcome');
    cy.get('.ds-cloud-btn, [aria-label="Partager un fichier"]').click();
    cy.url().should('include', '/upload');
  });

  it('permet un upload anonyme et affiche le lien', () => {
    cy.visit('/upload');
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('fichier anonyme'),
        fileName: 'anon.txt',
        mimeType: 'text/plain'
      },
      { force: true }
    );
    cy.contains('button', /Téléverser/).click();
    cy.wait('@uploadAnonymous');
    cy.contains(/Lien de téléchargement/i).should('be.visible');
  });
});
