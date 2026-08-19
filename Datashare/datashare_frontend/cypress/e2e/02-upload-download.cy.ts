/**
 * US01 / US02 — Upload + download RÉELS (fichier en base + disque)
 */
import { UploadPage } from '../pages/UploadPage';
import { HomePage } from '../pages/HomePage';

describe('US01 / US02 — Upload & download réels', () => {
  const uploadPage = new UploadPage();
  const homePage = new HomePage();
  let token = '';
  let email = '';

  before(function () {
    if (Cypress.env('mode') === 'mock') this.skip();
  });

  beforeEach(() => {
    cy.ensureUser().then((u) => {
      email = u.email;
      token = u.token;
    });
  });

  it('upload multipart UI puis retrouve le fichier dans Mes fichiers', () => {
    uploadPage.visitWithAuth(token).selectFixture().submit().expectSuccessLink();

    homePage.visitWithAuth(token).shouldList('test.pdf');
  });

  it('metadata + POST download via API réelle', () => {
    // Upload d’abord via UI pour avoir un token
    uploadPage.visitWithAuth(token).selectFixture().submit().expectSuccessLink();

    cy.apiUrl().then((base) => {
      cy.request({
        method: 'GET',
        url: `${base}/api/files`,
        headers: { Authorization: `Bearer ${token}` }
      }).then((res) => {
        expect(res.status).to.eq(200);
        expect(res.body.length).to.be.greaterThan(0);
        const file = res.body[0];
        const dlToken = file.downloadToken || file.download_token;

        cy.request({
          method: 'GET',
          url: `${base}/api/files/metadata/${dlToken}`
        }).then((meta) => {
          expect(meta.status).to.eq(200);
        });

        // Download binaire = POST (aligné code)
        cy.request({
          method: 'POST',
          url: `${base}/api/files/download/${dlToken}`,
          encoding: 'binary',
          failOnStatusCode: false
        }).then((dl) => {
          expect(dl.status).to.eq(200);
          expect(Number(dl.body?.length || 0)).to.be.greaterThan(0);
        });
      });
    });
  });
});
