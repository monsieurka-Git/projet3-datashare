/**
 * Scénario E2E 3 — Suppression d'un fichier (US06)
 */
describe('US06 — Suppression de fichier', () => {
  beforeEach(() => {
    cy.clearAllLocalStorage();
    cy.mockAuthApi();
    cy.mockFilesApi();      // crée @listFiles, @metadata, @download, @upload, @deleteFile
    cy.visitHomeAsUser();   // login + redirection vers /home + attend @listFiles
  });

  it('demande confirmation puis supprime le fichier de la liste', () => {
    // Le fichier mocké dans mockFilesApi doit apparaître
    cy.contains('rapport.pdf', { timeout: 10000 }).should('be.visible');

    // Bouton supprimer
    cy.contains('.ds-file-row', 'rapport.pdf')
      .contains('button', /Supprimer/i)
      .click();

    // Modale de confirmation
    cy.contains(/Confirmer la suppression|vraiment supprimer/i).should('be.visible');
    cy.contains('button', /Confirmer/i).should('be.visible');
    cy.contains('button', /Annuler/i).should('be.visible');

    // Confirmer
    cy.contains('button', /Confirmer/i).click();

    // Attendre la requête mockée
    cy.wait('@deleteFile');

    // La modale doit disparaître
    cy.contains(/Confirmer la suppression|vraiment supprimer/i).should('not.exist');

    // La liste doit être rechargée
    cy.wait('@listFiles').then(() => {
      // Le fichier ne doit plus être présent
      cy.contains('rapport.pdf').should('not.exist');
    });
  });

  it('annule la suppression via la modale', () => {
    cy.contains('rapport.pdf', { timeout: 10000 }).should('be.visible');

    cy.contains('.ds-file-row', 'rapport.pdf')
      .contains('button', /Supprimer/i)
      .click();

    // Annuler
    cy.contains('button', /Annuler/i).click();

    // Le fichier doit toujours être visible
    cy.contains('rapport.pdf').should('be.visible');
  });
});
