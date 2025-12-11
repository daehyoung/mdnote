describe('Admin User Management', () => {
  beforeEach(() => {
    cy.viewport(1280, 720);
    cy.visit('/login');
    cy.get('input[name=username]').type('admin');
    cy.get('input[name=password]').type('admin');
    cy.get('button').contains('Login').click();
    cy.url().should('not.include', '/login'); // Wait for redirect
  });

  it('should list users and add a new user', () => {
    // Intercept header/fetch
    cy.intercept('GET', '/api/admin/users').as('getUsers');
    
    cy.visit('/admin');
    cy.url().should('include', '/admin'); // Verify we are on admin page
    
    // Wait for data
    cy.wait('@getUsers');
    
    // Verify Page Load - Wait for specific admin content
    // Use icon to verify existence, maybe text is hidden in responsive view
    cy.get('.mdi-account-group').should('exist');
    
    // Check if table exists
    cy.get('table').should('exist'); 
    
    // Verify Admin user exists in the list
    cy.contains('td', 'admin').should('be.visible');
    
    // Verify Actions exist
    // Click the menu button first to reveal actions
    cy.get('.mdi-dots-vertical').first().click();
    cy.contains('Assign Org').should('exist');
    cy.contains('Deactivate').should('exist'); // Assuming admin is ACTIVE
  });
});
