describe('Document Management', () => {
  beforeEach(() => {
    // Perform login
    cy.visit('/login');
    cy.get('input[name="username"]').type('test');
    cy.get('input[name="password"]').type('test');
    cy.get('button').contains('Login').click();
    
    // Wait for App to load
    cy.get('.v-toolbar-title', { timeout: 10000 }).should('contain', 'MD Note');
  });

  it('creates, reads, and deletes a document', () => {
    const title = `e2e_test_${Date.now()}`;
    
    // Toggle EDIT mode
    cy.get('[data-test="mode-edit"]').click({ force: true });
    
    // Create
    cy.contains('New Document').click({ force: true });
    
    // Should be in edit view
    cy.url().should('include', '/edit');
    
    // Use clear() before type() if needed, but for new doc it's 'Untitled'
    cy.get('[data-test="title-input"]').find('input').clear({ force: true }).type(title, { force: true });
    cy.get('textarea', { timeout: 10000 }).should('be.visible').clear({ force: true }).type('# hello world', { force: true });
    
    // Wait for the dirty flag/save button to enable
    cy.get('[data-test="save-button"]').should('not.be.disabled').click({ force: true });
    
    // Explicit wait for backend persistence/store sync
    cy.wait(2000);
    
    // Verify saved (button becomes disabled after save)
    cy.get('[data-test="save-button"]').should('be.disabled');
    
    // Verify persistence by reloading the page
    cy.reload();
    cy.get('[data-test="title-input"]', { timeout: 10000 }).find('input').should('have.value', title);
    cy.get('textarea').should('have.value', '# hello world');

    // Delete
    cy.get('[data-test="delete-button"]').click({ force: true });
    cy.on('window:confirm', () => true);
    
    cy.url().should('eq', Cypress.config().baseUrl + '/');
    
    // Searching for deleted title should show "not found"
    cy.get('#search-input').find('input').clear({ force: true }).type(title + '{enter}', { force: true });
    cy.contains(title).should('not.exist');
  });
});
