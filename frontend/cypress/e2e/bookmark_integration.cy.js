describe('Bookmark Integration Test', () => {
    const testTitle = 'Bookmark Test Doc ' + Date.now();
  
    before(() => {
        // Setup: Create a document to "bookmark"
        cy.viewport(1280, 720);
        cy.visit('/login');
        cy.get('input[name=username]').type('admin');
        cy.get('input[name=password]').type('admin');
        cy.get('button').contains('Login').click();
        
        // Toggle EDIT mode
        cy.get('button[value="EDIT"]').click();

        // Create Doc
        cy.contains('New Document').click();
        cy.get('textarea[placeholder="Type your markdown here..."]').should('be.visible');
        cy.get('#title-input').clear().type(testTitle);
        cy.get('#content-editor').clear().type('Content for bookmark test');
        cy.get('[data-test="save-button"]').click();
        cy.get('[data-test="save-button"]').should('be.disabled'); // Wait for save
    });
  
    it('should successfully load a document directly via URL (Simulating Bookmark)', () => {
        // 1. Get the current URL (which is the document URL)
        cy.url().then((url) => {
            const docUrl = url;
            cy.log('Bookmark URL:', docUrl);
            expect(docUrl).to.include('/documents/');

            // 2. Simulate closing tabs/returning later (Go Home first)
            cy.visit('/');
            cy.url().should('not.include', '/documents/');

            // 3. Visit the "Bookmarked" URL
            cy.visit(docUrl);

            // 4. Verify the document is loaded correctly
            // Use contains because the app might be in VIEW mode by default when visiting directly
            cy.contains(testTitle, { timeout: 10000 }).should('be.visible');
            cy.contains('Content for bookmark test').should('be.visible');
        });
    });
});
