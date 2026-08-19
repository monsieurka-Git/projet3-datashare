/**
 * US05 — Historique / Mes fichiers (page /home)
 */
import { UploadPage } from '../pages/UploadPage';
import { HomePage } from '../pages/HomePage';

describe('US05 — Mes fichiers réel', () => {
  const uploadPage = new UploadPage();
  const homePage = new HomePage();
  let token = '';

  before(function () {
    if (Cypress.env('mode') === 'mock') this.skip();
  });

  beforeEach(() => {
    cy.ensureUser().then((u) => {
      token = u.token;
    });
  });

  it('affiche le fichier uploadé sur /home', () => {
    uploadPage.visitWithAuth(token).selectFixture().submit().expectSuccessLink();
    homePage.visitWithAuth(token).shouldList('test.pdf');
  });
});
