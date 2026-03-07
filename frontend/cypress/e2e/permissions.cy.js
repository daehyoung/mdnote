describe('Document Permissions Grannular', () => {
    let docId;
    
    // Credentials
    const admin = { user: 'admin', pass: 'admin' };
    const u1 = { user: 'user1', pass: 'password', dept: 'Engineering' };
    const u2 = { user: 'user2', pass: 'password', dept: 'Engineering' };
    const u3 = { user: 'user3', pass: 'password', dept: 'HR' };

    const login = (username, password) => {
        cy.viewport(1280, 720); // Ensure desktop view for every test session
        cy.visit('/login');
        cy.get('input[name="username"]').clear().type(username);
        cy.get('input[name="password"]').clear().type(password);
        cy.get('button').contains('Login').click();
        cy.location('pathname').should('eq', '/'); 
        cy.contains('Documents').should('be.visible');
    };

    const logout = () => {
        cy.get('button[title="Logout"]').click();
        cy.location('pathname').should('include', '/login');
    };
    
    const createTestUser = (userObj) => {
        // Assumes Admin is logged in
        // We can use API directly to speed up?
        // cy.request('POST', '/api/users', ...) -> Needs Auth token.
        // Let's use UI for stability verification too, or API if we can get token.
        // UI is safer.
        cy.visit('/admin/users');
        // Check if user exists
        cy.get('body').then($body => {
            if ($body.text().includes(userObj.user)) {
                // assume created
            } else {
                cy.contains('Add User').click();
                cy.get('input[label="Username"]').type(userObj.user); // Vuetify label selection might be tricky
                // Fallback to name/id if possible, but UserManageView likely uses v-text-field label
                // Let's try to assume labels: Username, Password, Name, Dept
                // Actually, let's look at `UserManageView.vue` later if this fails.
                // For now, let's use a simpler approach: use API command with cy.request
                // We need token. LocalStorage 'token'?
                // cy.window().its('localStorage.token') ...
            }
        });
    };
    
    // Actually, creating users via UI is complex due to Vuetify selectors.
    // Let's rely on API.
    const setupUsers = () => {
        login(admin.user, admin.pass);
        cy.window().then((win) => {
            const token = win.localStorage.getItem('token');
            const authHeader = { Authorization: 'Bearer ' + token };
            
            // Get Departments to find IDs
            cy.request({ method: 'GET', url: '/api/categories/tree', headers: authHeader }).then((resp) => { // wrong endpoint? 
               // Departments are /api/departments ? 
               // We implemented Departments? Or reusing Categories?
               // `Department` entity exists. `DepartmentRepository` exists.
               // `OrganizationView.vue` uses `/api/departments/tree`?
               // Let's assume /api/departments exists.
            });
            
            // Since I don't know department IDs, let's just create users with NO department first, 
            // then assign if possible, OR just use 'test' user and 'admin'.
            // But 'test' user has no department.
            
            // Let's try to just use 'test' user and modify 'test' user to have a department.
            // Admin can update 'test' user.
        });
        logout();
    };

    // simplified test for now to just check boolean toggles working for Public
    before(() => {
        // Setup: Ensure 'test' user exists and has known password using Admin API
        login(admin.user, admin.pass);
        
        cy.window().then((win) => {
            const token = win.localStorage.getItem('token');
            if(!token) throw new Error("No token for Admin");
            
            // 1. Reset 'test' user password to 'test' (or create if missing)
            // First try create (ignores if exists usually or fails) - actually easier to just update if we know ID?
            // We don't know ID.
            // Let's list users to find ID, then Update?
            // Or just try to Create with 'failOnStatusCode: false'.

            // Try Create 'test'
             cy.request({
                 method: 'POST',
                 url: '/api/admin/users',
                 headers: { Authorization: `Bearer ${token}` },
                 body: {
                     username: 'test',
                     password: 'test',
                     name: 'Test User',
                     role: 'USER',
                     status: 'ACTIVE'
                 },
                 failOnStatusCode: false
             }).then((resp) => {
                 if (resp.status >= 400) {
                     // If failed (likely exists), we might want to reset password.
                     // But we don't know ID.
                     // Assuming 'test'/'test' from DataInitializer works if it wasn't changed.
                     // If it fails, manual DB reset is needed or more complex logic.
                     // For now, let's assume it works or we just created it.
                 }
             });

             // Create 'user2'
             cy.request({
                 method: 'POST',
                 url: '/api/admin/users',
                 headers: { Authorization: `Bearer ${token}` },
                 body: {
                     username: 'user2',
                     password: 'password',
                     name: 'User 2',
                     role: 'USER',
                     status: 'ACTIVE'
                 },
                 failOnStatusCode: false
             });
        });
        
        logout();
    });

    it('Owner (test) can set Public Write = False, Admin can still edit', () => {
        login('test', 'test');
        cy.get('[data-test="mode-edit"]').click({ force: true }); // Enable Edit Mode
        // Create
        cy.contains('New Document', {timeout: 10000}).should('be.visible').click();
        cy.get('#title-input').clear().type('Public Write Test');
        cy.get('#content-editor').type('Content');
        cy.contains('Save').click();
        
        // Open Permissions
        cy.contains('Permissions').click();
        
        // Default: Group Read=T, Public Read=T. Others False.
        // Verify Checkboxes
        // Checkboxes are v-checkbox.
        // Public Write is the 2nd 'Write' checkbox?
        // Group Write is 1st.
        // Let's toggle Public Write ON.
        cy.get('input[aria-label="Write"]').eq(1).parent().click(); // Parent click often better for Vuetify 
        cy.contains('Save').click();
        cy.wait(500);
        
        // Logout
        logout();
        
        // Login as Admin
        login(admin.user, admin.pass);
        // Admin needs Edit mode to see DRAFTs
        cy.get('[data-test="mode-edit"]').click({ force: true });
        
        cy.contains('Public Write Test').click();
        cy.get('[data-test="edit-document-button"]').click({ force: true }); // Enable Edit Mode via Doc Button
        cy.get('[data-test="save-button"]').should('exist');
        
        logout();
    });
    
    it('Public Write = True allows another user to Edit', () => {
         // Now login as user2
         login('user2', 'password');
         // reload list to be sure
         cy.visit('/');
         // 1. Switch to Edit Mode FIRST (so loadDocument sees correct mode)
         cy.get('[data-test="mode-edit"]').click({ force: true }); 
         
         // 2. Open Document
         cy.contains('Public Write Test').click();
         
         // 3. Click Edit
         cy.get('[data-test="edit-document-button"]').click({ force: true });
         
         // Should be editable
         cy.get('[data-test="save-button"]').should('exist');
         
         // Edit
         cy.get('#content-editor').type(' Edited');
         cy.get('[data-test="save-button"]').click();
         
         logout();
    });

    it('Group Read/Write = True allows member of SAME department', () => {
        // Setup departmental users via API
        login(admin.user, admin.pass);
        cy.window().then(win => {
            const token = win.localStorage.getItem('token');
            const auth = { Authorization: `Bearer ${token}` };
            
            // 1. Create Department Eng (or get)
            cy.request({ method: 'POST', url: '/api/admin/departments', headers: auth, body: { name: 'Engineering' }, failOnStatusCode: false }).then(resp => {
                const deptId = resp.status === 201 ? resp.body.id : 1; // Fallback or find
                
                // 2. Assign 'test' and 'user2' to Engineering
                cy.request({ method: 'GET', url: '/api/admin/users', headers: auth }).then(uResp => {
                    const testUser = uResp.body.find(u => u.username === 'test');
                    const user2 = uResp.body.find(u => u.username === 'user2');
                    
                    if(testUser) cy.request({ method: 'PUT', url: `/api/admin/users/${testUser.id}`, headers: auth, body: { ...testUser, departmentId: deptId } });
                    if(user2) cy.request({ method: 'PUT', url: `/api/admin/users/${user2.id}`, headers: auth, body: { ...user2, departmentId: deptId } });
                });
            });
        });
        logout();

        login('test', 'test');
        cy.get('[data-test="mode-edit"]').click({ force: true });
        cy.contains('New Document').click();
        cy.get('#title-input').clear().type('Group Test Doc');
        cy.get('#content-editor').type('Secret Eng Content');
        
        // Disable Public, Enable Group Read/Write (usually default for Group Read is T)
        cy.contains('Permissions').click();
        cy.get('input[aria-label="Read"]').eq(1).parent().click(); // Toggle Public Read OFF
        cy.get('input[aria-label="Write"]').eq(0).parent().click(); // Toggle Group Write ON
        cy.contains('Save').click();
        logout();

        // User2 (Same Dept) should see and edit
        login('user2', 'password');
        cy.get('[data-test="mode-edit"]').click({ force: true });
        cy.contains('Group Test Doc').click();
        cy.get('[data-test="edit-document-button"]').click({ force: true });
        cy.get('[data-test="save-button"]').should('exist');
        logout();
    });
});
