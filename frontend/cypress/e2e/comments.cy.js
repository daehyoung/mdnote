describe('Document Comments Flow', () => {
    const admin = { user: 'admin', pass: 'admin' };
    const u1 = { user: 'test', pass: 'test' };
    const u2 = { user: 'user2', pass: 'password' };

    const login = (username, password) => {
        cy.visit('/login');
        cy.get('input[name="username"]').type(username);
        cy.get('input[name="password"]').type(password);
        cy.get('button').contains('Login').click();
    };

    const logout = () => {
        cy.get('button[title="Logout"]').click();
    };

    it('registers a comment and owner can delete it', () => {
        login(u1.user, u1.pass);
        cy.get('[data-test="mode-edit"]').click({ force: true });
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('Comment Test Doc');
        cy.contains('Save').click();
        
        // Ensure Allow Comments is ON (default)
        cy.contains('Permissions').click();
        // Check switch state if needed, or just proceed
        
        // Go back to list or detail
        cy.get('[data-test="back-button"]').click();
        cy.contains('Comment Test Doc').click();

        // Post Comment
        cy.get('input[placeholder="Write a comment..."]').type('This is a test comment{enter}');
        cy.contains('This is a test comment').should('be.visible');
        
        // Should see delete button
        cy.get('button').find('.mdi-delete').should('exist');
        
        logout();
    });

    it('Another user can see comment but NOT delete it', () => {
        login(u2.user, u2.pass);
        cy.contains('Comment Test Doc').click();
        cy.contains('This is a test comment').should('be.visible');
        
        // Should NOT see delete button for others' comments
        // Note: depends on implementation of removeComment visibility
        cy.get('button').find('.mdi-delete').should('not.exist');
        
        logout();
    });

    it('Admin can delete any comment', () => {
        login(admin.user, admin.pass);
        cy.contains('Comment Test Doc').click();
        cy.contains('This is a test comment').should('be.visible');
        
        // Admin sees delete
        cy.get('button').find('.mdi-delete').click();
        cy.on('window:confirm', () => true);
        
        cy.contains('This is a test comment').should('not.exist');
        logout();
    });
});
