describe('Document Ownership', () => {
    const adminUser = { username: 'admin', password: 'admin' };
    const testUser = { username: 'test', password: 'test' };
    
    // We need a third user to verify non-ownership access denial
    // but typically we use 'test' as 'author' and verify 'admin' access, or vice versa?
    // Let's assume 'test' creates a doc, ensuring 'test' can edit, and 'admin' can edit.
    // We need a 'user2' to verify denial.
    // For now, let's verify:
    // 1. 'test' creates a document.
    // 2. 'test' sees Edit/Delete buttons.
    // 3. 'test' logs out.
    // 4. 'admin' logs in, sees Edit/Delete buttons (ADMIN override).
    
    // To verify denial, we'd need another standard user. We can use 'admin' creating a doc and 'test' trying to delete it?
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
        cy.contains('button', 'Edit').click({ force: true });
        cy.contains('New Document').should('be.visible');
    };

    it('User can edit own document', () => {
        login(testUser.username, testUser.password);
        
        // Create Doc
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('User Owned Doc');
        cy.get('#content-editor').type('My Content');
        cy.contains('Save').click();
        cy.contains('button', 'Edit').should('be.visible'); // Is in Edit Mode
        
        // Verify Edit/Delete buttons visible
        cy.get('[data-test="save-button"]').should('exist');
        cy.get('.mdi-delete').should('exist');
    });

    it('Non-owner cannot delete Admin document', () => {
        // 1. Admin creates doc
        login(adminUser.username, adminUser.password);
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('Admin Doc for Protection Test');
        cy.get('#content-editor').type('Protected Content');
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
        cy.contains('Admin Doc for Protection Test').click();
        
        // 4. Verify Delete Button NOT present
        cy.get('.mdi-delete').should('not.exist');
        
        // 5. Verify Save Button disabled or not present
        cy.get('[data-test="save-button"]').should('not.exist');
    });

    it('Admin can delete User document', () => {
        // 1. User creates doc
        login(testUser.username, testUser.password);
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('User Doc for Admin deletion');
        cy.contains('Save').click();
        cy.wait(500);

        cy.clearLocalStorage();
        cy.clearCookies();

        // 2. Admin logs in
        login(adminUser.username, adminUser.password);
        cy.contains('User Doc for Admin deletion').click();
        
        // 3. Admin sees delete button
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
        cy.contains('User Doc for Admin deletion').should('not.exist');
    });
});
