export class LoginPage {
  visit(): this {
    cy.visit('/login');
    return this;
  }

  loginAs(email: string, password: string): this {
    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type(password);
    cy.contains('button', /Connexion/i).click();
    return this;
  }
}
