describe('Search Functionality', () => {
  beforeEach(() => {
    cy.viewport(1280, 720);
    cy.visit('/login');
    cy.get('input[name=username]').type('admin');
    cy.get('input[name=password]').type('admin');
    cy.get('button').contains('Login').click();
  });

  it('should filter documents by search query', () => {
    // Ensure we have some docs
    // We can rely on existing docs or create one
    const uniqueTitle = 'Searchable Doc ' + Date.now();
    
    // Create doc
    cy.get('button[value="EDIT"]').click({ force: true });
    cy.contains('New Document').click();
    cy.get('#title-input').clear().type(uniqueTitle);
    cy.contains('Save').click();
    
    // Go back to list (or reload)
    // cy.reload() keeps us on the document page now that we have bookmarkable URLs.
    // We must go to root to see the list and search bar
    cy.visit('/');
    
    // Type in search bar
    cy.get('#search-input').type(uniqueTitle);
    
    // Verify list contains only the doc (or at least contains it)
    cy.contains(uniqueTitle).should('be.visible');
    
    // Verify non-matching doc is hidden (if possible, need known other doc)
    // cy.contains('Other Doc').should('not.exist');
  });
});
