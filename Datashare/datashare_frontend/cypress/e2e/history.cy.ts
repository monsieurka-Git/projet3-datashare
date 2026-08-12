/**
 * History / Mes fichiers
 */
describe('History Page', () => {
  beforeEach(() => {
    cy.clearAllLocalStorage();
    cy.mockAuthApi();
    cy.mockFilesApi();
  });

  it('affiche la liste des fichiers', () => {
    cy.visitHomeAsUser();
    cy.contains(/Mes fichiers/i).should('be.visible');
    cy.contains('rapport.pdf').should('be.visible');
  });

  it('supprime un fichier', () => {
    cy.visitHomeAsUser();
    cy.contains('rapport.pdf').should('be.visible');
    cy.contains('.ds-file-row', 'rapport.pdf')
      .contains('button', /Supprimer/i)
      .click();
    cy.contains('button', /Confirmer/i).click();
    cy.wait('@deleteFile');
    cy.wait('@listFiles');
    cy.contains('rapport.pdf').should('not.exist');
  });
});
