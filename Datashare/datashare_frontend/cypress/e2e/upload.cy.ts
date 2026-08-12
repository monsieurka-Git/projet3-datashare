describe('Upload Page', () => {

  beforeEach(() => {
    cy.mockFilesApi();
    cy.visit('/upload');
  });

  it('affiche les champs nécessaires', () => {
    cy.get('input[type="file"]').should('exist');
    cy.get('input[type="password"]').should('exist');
    // Le bouton de upload est type="button" avec (click)="onUpload()"
    cy.contains('button', /Téléverser/).should('exist');
  });

  it('refuse un mot de passe trop court', () => {
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('test'),
        fileName: 'test.pdf',
        mimeType: 'application/pdf'
      },
      { force: true }
    );
    cy.get('input[type="password"]').type('abc12'); // 5 caractères
    cy.contains('button', /Téléverser/).click();

    cy.contains('Le mot de passe doit contenir au moins 6 caractères').should('be.visible');
  });

  it('refuse un mot de passe non alphanumérique', () => {
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('test'),
        fileName: 'test.pdf',
        mimeType: 'application/pdf'
      },
      { force: true }
    );
    cy.get('input[type="password"]').type('abc123!');
    cy.contains('button', /Téléverser/).click();

    cy.contains('Le mot de passe doit être alphanumérique').should('be.visible');
  });

  it('upload un fichier avec succès', () => {
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('test'),
        fileName: 'test.pdf',
        mimeType: 'application/pdf'
      },
      { force: true }
    );
    cy.get('input[type="password"]').type('abc123');
    cy.contains('button', /Téléverser/).click();

    cy.contains('Fichier téléversé avec succès').should('be.visible');
  });

});
