/**
 * Scénario E2E 2 — Upload de fichier et accès au lien (US01, US02)
 */
describe('US01 / US02 — Upload et téléchargement', () => {
  beforeEach(() => {
    cy.clearAllLocalStorage();
    cy.mockAuthApi();
    cy.mockFilesApi(); // crée @files, @metadata, @download, @upload
  });

  it('upload un fichier et affiche le lien de téléchargement', () => {
    cy.visit('/upload', {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          'token',
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEifQ.sig'
        );
        win.localStorage.setItem('userId', '11111111-1111-1111-1111-111111111111');
      }
    });

    cy.contains(/Ajouter un fichier|Partager un fichier/).should('be.visible');

    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('Contenu E2E DataShare'),
        fileName: 'rapport-e2e.pdf',
        mimeType: 'application/pdf'
      },
      { force: true }
    );

    cy.contains('rapport-e2e.pdf').should('be.visible');

    cy.get('input[type="number"]').clear().type('5');
    cy.contains('button', /Téléverser/).click();

    cy.wait('@upload'); // mockFilesApi crée ce stub

    cy.contains(/Lien de téléchargement/i).should('be.visible');
    cy.get('a[href*="/download/"]').should('exist');
  });

  it('retrouve le fichier dans Mes fichiers et accède au download', () => {
    cy.visitHomeAsUser(); // ton helper qui fait login + redirection + attend @listFiles

    cy.contains(/Mes fichiers/i).should('be.visible');
    cy.contains('rapport.pdf', { timeout: 10000 }).should('be.visible');

    // Le bouton "Accéder" est mocké dans mockFilesApi → toujours présent
    cy.contains('button', /Accéder/i).first().click();

    cy.url().should('include', '/download/');
    cy.wait('@metadata');

    cy.contains(/Télécharger un fichier/i).should('be.visible');
    cy.contains('rapport.pdf').should('be.visible');

    cy.contains('button', /Télécharg/i).click();
    cy.wait('@download');
  });

  it('page download affiche les métadonnées sans authentification', () => {
    cy.clearAllLocalStorage();
    cy.mockFilesApi();

    cy.visit('/download/download-token-e2e-1234');

    cy.wait('@metadata');

    cy.contains('rapport.pdf').should('be.visible');
    cy.contains(/Taille/i).should('be.visible');
  });
});
