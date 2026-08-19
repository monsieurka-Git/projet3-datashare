export class UploadPage {
  visitWithAuth(token: string, userId?: string): this {
    cy.visit('/upload', {
      onBeforeLoad(win) {
        win.localStorage.setItem('token', token);
        if (userId) win.localStorage.setItem('userId', userId);
      }
    });
    return this;
  }

  visitAnonymous(): this {
    cy.visit('/upload');
    return this;
  }

  selectFixture(path = 'cypress/fixtures/test.pdf'): this {
    cy.get('input[type="file"]').selectFile(path, { force: true });
    return this;
  }

  submit(): this {
    cy.contains('button', /Téléverser/i).click();
    return this;
  }

  expectSuccessLink(): this {
    cy.contains(/Lien de téléchargement|téléchargement/i, { timeout: 20000 }).should('be.visible');
    return this;
  }
}
