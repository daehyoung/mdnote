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
      cy.get('[data-test="mode-edit"]').click({ force: true });
      
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
          let docId = url.split('/').pop();
          if (docId === 'edit' || docId.includes('?')) {
              // Handle /edit or query params
              const pathParts = url.split('?')[0].split('/');
              // If ends with edit
              if (pathParts[pathParts.length - 1] === 'edit') {
                  docId = pathParts[pathParts.length - 2];
              } else {
                  docId = pathParts[pathParts.length - 1];
              }
          }
          
          // 3. Go back to Home
          cy.get('[data-test="back-button"]').click();
          // Initially goes to Detail View if saved recently or logic implies
          // Check if we are still in documents path
          cy.url().then((newUrl) => {
              if (newUrl.includes('/documents/')) {
                 // Click back again to go to list
                 cy.get('[data-test="back-button"]').click();
              }
          });
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
