describe('Login Flow', () => {
  it('successfully logs in with valid credentials', () => {
    cy.visit('/login'); 

    cy.get('input[name="username"]').type('test');
    cy.get('input[name="password"]').type('test');
    cy.get('button').contains('Login').click();

    // Should redirect to home
    cy.url().should('eq', Cypress.config().baseUrl + '/');
    cy.get('.v-toolbar-title').should('contain', 'MD Note');
  });

  it('fails with invalid credentials', () => {
    cy.visit('/login');
    cy.get('input[name="username"]').type('wrong');
    cy.get('input[name="password"]').type('wrong');
    cy.get('button').contains('Login').click();

    cy.on('window:alert', (str) => {
      expect(str).to.equal('Authentication failed');
    });
  });
});
