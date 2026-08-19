export class HomePage {
  visitWithAuth(token: string, userId?: string): this {
    cy.visit('/home', {
      onBeforeLoad(win) {
        win.localStorage.setItem('token', token);
        if (userId) win.localStorage.setItem('userId', userId);
      }
    });
    return this;
  }

  shouldList(name: string): this {
    cy.contains(name, { timeout: 15000 }).should('be.visible');
    return this;
  }
}
