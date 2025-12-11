describe('Document Ownership', () => {
    const adminUser = { username: 'admin', password: 'admin' };
    const testUser = { username: 'test', password: 'test' };
    
    // We need a third user to verify non-ownership access denial
    // but typically we use 'test' as 'author' and verify 'admin' access, or vice versa?
    // Let's assume 'test' creates a doc, ensuring 'test' can edit, and 'admin' can edit.
    // We need a 'user2' to verify denial.
    // For now, let's verify:
    // 1. 'test' creates a document.
    // Use unique timestamp to prevent stale data from failing assertions
    const timestamp = Date.now();
    const adminDocTitle = `Admin Doc for Protection Test ${timestamp}`;
    const userDocTitle = `User Doc for Admin deletion ${timestamp}`;
    // Yes, 'admin' is author='admin'. 'test' is NOT admin and NOT 'admin'.
    
    const login = (username, password) => {
        cy.viewport(1280, 720);
        cy.visit('/login');
        cy.get('input[name=username]').type(username);
        cy.get('input[name=password]').type(password);
        cy.get('button').contains('Login').click();
        cy.url().should('not.include', '/login');
        cy.contains('Documents').should('be.visible');
        // Ensure Edit mode is active
        cy.get('[data-test="mode-edit"]').click({ force: true });
        cy.contains('New Document').should('be.visible');
    };

    it('User can edit own document', () => {
        login(testUser.username, testUser.password);
        
        // Create Doc
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('User Owned Doc');
        cy.get('#content-editor').type('My Content');
        cy.contains('Save').click();
        
        // DocumentEditView stays on page after save.
        // We need to go back to DetailView to verify "Edit" button existence from there
        cy.get('[data-test="back-button"]').click();
        
        cy.url().should('include', '/documents/');
        cy.get('[data-test="edit-document-button"]').click(); // Go back to Edit Mode
        
        cy.get('[data-test="edit-document-button"]').should('not.exist'); // Wait, toggle is in HomeView only? No, global navigation?
        // App.vue usually has toolbar? No, HomeView has the toggle.
        // DetailView has specific "Edit" button.
        
        // Verify Edit/Delete buttons visible
        cy.get('[data-test="save-button"]').should('exist');
        cy.get('.mdi-delete').should('exist');
    });

    it('Non-owner cannot delete Admin document', () => {
        // 1. Admin creates doc separate from others
        login(adminUser.username, adminUser.password);
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type(adminDocTitle);
        cy.get('#content-editor').type('Protected Content');
        
        // PUBLISH it so other users can see it
        cy.get('#status-selector').parent().click(); // Open dropdown
        cy.contains('PUBLISHED').click(); // Select
        
        cy.contains('Save').click();
        
        cy.window().then((win) => {
             // wait for save
             cy.wait(1000);
        });

        // 2. Login as Test User
        cy.clearLocalStorage();
        cy.clearCookies();
        
        login(testUser.username, testUser.password);
        
        // 3. Find Admin Doc
        cy.contains(adminDocTitle).click();
        
        // 4. Verify Delete Button NOT present
        // In Detail View, Delete is NOT present regardless.
        // User shouldn't see "Edit" button either if no permission.
        cy.get('[data-test="edit-document-button"]').should('not.exist');
        cy.get('.mdi-delete').should('not.exist');
        
        // 5. Verify Save Button disabled or not present
        cy.get('[data-test="save-button"]').should('not.exist');
    });

    it('Admin can delete User document', () => {
        // 1. User creates doc
        login(testUser.username, testUser.password);
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type(userDocTitle);
        cy.contains('Save').click();
        
        // DocumentEditView stays on page after save.
        // Go back to list to ensure it's there? Or logout/re-login implies list visit.
        
        cy.wait(500);

        cy.clearLocalStorage();
        cy.clearCookies();

        // 2. Admin logs in
        login(adminUser.username, adminUser.password);
        // Admin needs Edit mode to see DRAFTs (if user didn't publish)
        cy.get('[data-test="mode-edit"]').click({ force: true });
        
        cy.contains(userDocTitle).click();
        
        // 3. Admin sees delete button
        // Must go to Edit View
        cy.get('[data-test="edit-document-button"]').click();
        
        cy.get('.mdi-delete').should('exist');
        cy.get('.mdi-delete').click({force: true});
        
        // Confirm
        // cy.on('window:confirm', () => true); // Cypress auto-accepts? No, we need to stub.
        // But in HomeView: if (!confirm(...)) return;
        // Cypress by default returns false for confirm? No, it returns true?
        // "By default, Cypress will automatically accept alerts and confirmations." -> TRUE.
        // So it should work.
        
        // Verify deleted
        cy.wait(1000);
        cy.contains(userDocTitle).should('not.exist');
    });
});
