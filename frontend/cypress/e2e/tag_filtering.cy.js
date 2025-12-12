describe('Tag Filtering', () => {
  const uniqueTag = `tag-${Date.now()}`;
  const docTitle = `Doc with ${uniqueTag}`;

  before(() => {
        // Login
        cy.visit('/login');
        cy.get('input[name=username]').type('admin');
        cy.get('input[name=password]').type('admin');
        cy.get('button').contains('Login').click();
        cy.url().should('not.include', '/login');
        
        // Create Document via API
        cy.window().then((win) => {
            const token = win.localStorage.getItem('token'); // or sessionStorage depending on auth store
            // Note: AuthStore might store in localStorage as 'user' or similar. 
            // Better to rely on UI creation if possible, but for Tags, let's try API if we can't find UI input.
            // If token is missing (httponly cookie?), we might need another way.
            // But let's assume we can use the UI to add tags if we find the input.
            // Actually, let's try to type #tag in content and see if backend parses it (common in md apps).
            
            // UI Approach with Hashtag
            // cy.contains('New Document').click();
            // cy.get('#title-input').clear().type(docTitle);
            // cy.get('#content-editor').clear().type(`Content with #${uniqueTag}`);
            // cy.get('[data-test="save-button"]').click();
            // cy.get('[data-test="save-button"]').should('be.disabled');
            
            // If hashtag parsing isn't implemented, this fails. 
            // Let's assume API is safer for "Tag Features" task if UI input isn't obvious.
            // DocumentService.createDocument -> processTags.
            // processTags checks doc.getTags().
            
            // Let's use API.
            // But getting token is tricky if HttpOnly. 
            // If basic auth or we are logged in, cy.request might automatically attach cookies.
            // Our backend uses JWT in header 'Authorization'.
            
            // Let's try to create doc using UI without tags, then add tag via API update?
            // Or just try API creation with cy.request assuming cookies are set?
        });
        
        // Actually, let's use a simpler approach:
        // Login, then use cy.request to create document.
        // We need the token. 
        // Let's assume the app stores it in `auth` item in localStorage.
        
        cy.window().then((win) => {
             const auth = JSON.parse(win.localStorage.getItem('auth') || '{}');
             const token = auth.token;
             
             if (token) {
                 cy.request({
                    method: 'POST',
                    url: '/api/documents',
                    headers: { 'Authorization': `Bearer ${token}` },
                    body: {
                        title: docTitle,
                        content: 'Test content',
                        tags: [{ name: uniqueTag }],
                        status: 'PUBLISHED' // Ensure it shows up
                    }
                 });
             } else {
                 // Fallback or fail
                 // Maybe type #tag in UI if parsing supported?
                 // Let's try UI creation and assume Backend parses #tag from content?
                 // (Checking DocumentService... processTags uses doc.getTags(). It doesn't seem to parse content).
             }
        });
        
        cy.reload(); // Reload to fetch new doc
    });
  after(() => {
    // Cleanup if possible, or rely on database reset/seed
    cy.deleteDocumentByTitle(docTitle);
  });

  beforeEach(() => {
    cy.login('admin', 'admin123');
    cy.visit('/');
  });

  it('should display the new tag in the sidebar with count', () => {
    // Sidebar should contain the tag
    cy.get('.v-navigation-drawer').within(() => {
       cy.contains(uniqueTag).should('be.visible');
       // Check for count (1) - assuming loose matching or regex if exact text varies
       // The text is "tagname (1)"
       cy.contains(`${uniqueTag} (1)`).should('be.visible');
    });
  });

  it('should filter documents when tag is clicked', () => {
    // Click the tag
    cy.get('.v-navigation-drawer').contains(uniqueTag).click();

    // Verify URL or State (optional, if router updates)
    // Verify Document List
    cy.get('.v-main').within(() => {
        cy.contains(docTitle).should('be.visible');
        // Ensure other docs are filtered out if we knew they didn't have the tag, 
        // but for now verifying presence is minimum.
    });

    // Verify "All Documents" is NOT active
    cy.get('.v-navigation-drawer').contains('All Documents').parent().should('not.have.class', 'v-list-item--active');
  });

  it('should clear tag filter when All Documents is clicked', () => {
    // Select tag first
    cy.get('.v-navigation-drawer').contains(uniqueTag).click();
    cy.wait(500); // UI settle

    // Click All Documents
    cy.get('.v-navigation-drawer').contains('All Documents').click();

    // Verify Tag is not selected (visual check might be hard depending on Vuetify class, 
    // but check if "All Documents" is active)
    cy.get('.v-navigation-drawer').contains('All Documents').parent().should('have.class', 'v-list-item--active');
  });
});
