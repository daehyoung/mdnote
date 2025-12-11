describe('Theme Management', () => {
  beforeEach(() => {
    // Login
    cy.visit('/login');
    cy.get('input[name=username]').type('admin');
    cy.get('input[name=password]').type('admin');
    cy.get('button').contains('Login').click();
    
    // Go to Profile
    cy.get('[title="My Profile"]').click({ force: true });
  });

  it('should toggle theme and persist', () => {
    // Verify default or current theme (assuming Light initially or we set it)
    // Vuetify 3 applies theme class to .v-application (which is usually on .v-app div or body)
    // Actually v-theme-provider or v-app
    
    // Verify page loaded
    cy.contains('h1', 'User Profile').should('be.visible');
    cy.contains('Personal Information').should('be.visible');
    
    // Find the toggle
    // It might be scrolled down?
    cy.scrollTo('bottom'); 
    cy.contains('Appearance').should('be.visible');
    
    // Click Dark
    cy.contains('button', 'Dark').click({ force: true });
    
    // Verify class on v-app
    // Wait for theme application
    cy.wait(1000); 
    // Vuetify 3 uses css variables mostly, but also classes like v-theme--dark
    cy.get('.v-application').should('have.class', 'v-theme--dark');
    
    // Click Light
    cy.contains('button', 'Light').click({ force: true });
    
    // Test persistence
    cy.contains('button', 'Dark').click({ force: true });
    cy.reload();
    // Should still be dark
    cy.get('.v-application').should('have.class', 'v-theme--dark');
    
    // Reset to Light for other tests
    cy.contains('button', 'Light').click({ force: true });
  });
});
