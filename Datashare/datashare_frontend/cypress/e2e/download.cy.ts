/**
 * Download page
 */
describe('Download Page', () => {
  beforeEach(() => {
    cy.clearAllLocalStorage();
    cy.mockFilesApi();
  });

  it('affiche les métadonnées du fichier', () => {
    cy.visit('/download/download-token-e2e-1234');
    cy.wait('@metadata');
    cy.contains(/Télécharger un fichier/i).should('be.visible');
    cy.contains('rapport.pdf').should('be.visible');
  });

  it('permet de télécharger le fichier', () => {
    cy.visit('/download/download-token-e2e-1234');
    cy.wait('@metadata');
    cy.contains('button', /Télécharg/).click();
    cy.wait('@download');
  });

  it('gère un token invalide', () => {
    cy.intercept('GET', '**/api/files/metadata/**', {
      statusCode: 404,
      body: { error: 'Lien invalide ou introuvable' }
    }).as('metadata404');

    cy.visit('/download/token-inexistant');
    cy.wait('@metadata404');
    cy.contains(/invalide|introuvable|expiré|erreur/i).should('be.visible');
  });
});
