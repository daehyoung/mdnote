describe('Login Flow', () => {
  it('should successfully log in', () => {
    cy.visit('/');
    // Check if we are redirected to login or can navigate there
    // If auth is required, we might be on login page already or need to click
    // Our router redirects to login if not auth.
    // But let's verify we are on login page by url or content
    cy.url().should('include', '/login');
    
    // Fill in credentials
    cy.get('input[name=username]').type('admin');
    cy.get('input[name=password]').type('admin');
    
    // Click login
    cy.get('button').contains('Login').click();
    
    // Should pass (assuming dummy auth works and redirects)
    // Note: Our backend has a dummy auth that accepts anything, returning dummy-token
    
    // Verify redirection to home
    cy.url().should('include', '/');
    cy.contains('MD Note').should('be.visible'); // Check for App Bar title
  });
});
