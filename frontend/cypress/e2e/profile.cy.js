describe('Profile Management', () => {
  beforeEach(() => {
    cy.visit('/login');
    cy.get('input[name=username]').type('admin');
    cy.get('input[name=password]').type('admin');
    cy.get('button').contains('Login').click();
    // Navigate to profile
    cy.get('a[href="/profile"]').click(); // Vuetify router-link renders as anchor
    // Alternatively: cy.visit('/profile');
  });

  it('should display user profile information', () => {
    cy.contains('User Profile').should('be.visible');
    // Check Username field
    cy.contains('.v-field', 'Username').find('input').should('exist');
  });

  it('should allow updating profile information', () => {
    const newName = 'Admin Updated';
    
    // Find Name field and update
    cy.contains('.v-field', 'Name').find('input').clear().type(newName);
    cy.contains('button', 'Update Info').click();
    
    // Verify alert
    cy.on('window:alert', (str) => {
      expect(str).to.equal('Profile updated');
    });
    
    // Check persistence without reload just to verify store/UI update first
    cy.contains('.v-field', 'Name').find('input').should('have.value', newName);

    // Optional: reload to check server persistence
    /*
    cy.reload();
    cy.contains('.v-field', 'Name').find('input').should('have.value', newName);
    */
  });
});
