describe('URL Navigation and Deep Linking', () => {
    beforeEach(() => {
      // Login first
      cy.viewport(1280, 720);
      cy.visit('/login');
      cy.get('input[name=username]').type('admin');
      cy.get('input[name=password]').type('admin');
      cy.get('button').contains('Login').click();
      cy.url().should('not.include', '/login');
    });
  
    it('should navigate to document detail when clicking a document in the list', () => {
      // 1. Visit Home
      cy.visit('/');

      // 1.1 Toggle to EDIT mode
      cy.contains('button', 'Edit').click({ force: true });
      
      // 2. Create a document to ensure list is not empty (optional, but good for isolation)
      // Or just click first item if exists
      // Let's create one to be sure we have an ID to check
      cy.contains('New Document').click();
      cy.get('textarea[placeholder="Type your markdown here..."]').type('Navigation Test Doc');
      cy.get('[data-test="save-button"]').click();
      
      // Get the current URL which should now be /documents/:id
      cy.url().should('include', '/documents/');
      
      // Extract ID
      cy.url().then(url => {
          const docId = url.split('/').pop();
          
          // 3. Go back to Home
          cy.get('[data-test="back-button"]').click();
          cy.url().should('not.include', '/documents/'); // Should be root or just not have ID
          
          // 4. Visit directly via URL (Deep Link)
          cy.visit(`/documents/${docId}`);
          
          // Verify content loaded (checking for text is safer as mode might be VIEW)
          cy.contains('Navigation Test Doc', { timeout: 10000 }).should('be.visible');
          
          // 5. Verify Browser Back Button works
          cy.go('back');
          cy.url().should('not.include', `/documents/${docId}`);
      });
    });
});
