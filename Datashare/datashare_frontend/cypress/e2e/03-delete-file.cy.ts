/**
 * US06 — Suppression réelle (disque + BDD)
 */
import { UploadPage } from '../pages/UploadPage';

describe('US06 — Suppression réelle', () => {
  const uploadPage = new UploadPage();
  let token = '';

  before(function () {
    if (Cypress.env('mode') === 'mock') this.skip();
  });

  beforeEach(() => {
    cy.ensureUser().then((u) => {
      token = u.token;
    });
  });

  it('upload puis DELETE /api/files/info/{id} puis liste vide ou sans ce fichier', () => {
    uploadPage.visitWithAuth(token).selectFixture().submit().expectSuccessLink();

    cy.apiUrl().then((base) => {
      cy.request({
        method: 'GET',
        url: `${base}/api/files`,
        headers: { Authorization: `Bearer ${token}` }
      }).then((res) => {
        expect(res.body.length).to.be.greaterThan(0);
        const id = res.body[0].id;

        cy.request({
          method: 'DELETE',
          url: `${base}/api/files/info/${id}`,
          headers: { Authorization: `Bearer ${token}` },
          failOnStatusCode: false
        }).then((del) => {
          expect([200, 204]).to.include(del.status);
        });

        cy.request({
          method: 'GET',
          url: `${base}/api/files`,
          headers: { Authorization: `Bearer ${token}` }
        }).then((res2) => {
          const stillThere = (res2.body as any[]).some((f) => f.id === id);
          expect(stillThere).to.eq(false);
        });
      });
    });
  });
});
