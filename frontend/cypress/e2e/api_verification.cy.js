describe('API Verification & Cleanup', () => {
    let token = null;

    before(() => {
        // Login to get token
        cy.request('POST', '/api/v1/auth/login', {
            username: 'admin',
            password: 'password'
        }).then((response) => {
            expect(response.status).to.eq(200);
            token = response.body.token;
        });
    });

    it('should clean up "Untitled" documents', () => {
        cy.request({
            method: 'GET',
            url: '/api/documents',
            headers: { Authorization: `Bearer ${token}` }
        }).then((response) => {
            expect(response.status).to.eq(200);
            // Handle Pagination
            const docs = response.body.content || response.body;
            // Filter "Untitled" or "Cypress Test Doc"
            const trash = docs.filter(d => d.title === 'Untitled' || d.title.startsWith('Cypress Test Doc'));
            
            cy.log(`Found ${trash.length} documents to clean up.`);
            
            trash.forEach(d => {
                cy.request({
                    method: 'DELETE',
                    url: `/api/documents/${d.id}`,
                    headers: { Authorization: `Bearer ${token}` }
                });
            });
        });
    });

    it('should Create, Read, Update, and Verify a document via API', () => {
        const testDoc = {
            title: 'API Verification Doc',
            content: '# Hello World\nThis is a test content.',
            status: 'DRAFT',
            tags: [{ name: 'api-test' }],
            attachments: []
        };

        let createdId = null;

        // 1. Create
        cy.request({
            method: 'POST',
            url: '/api/documents',
            headers: { Authorization: `Bearer ${token}` },
            body: testDoc
        }).then((response) => {
            expect(response.status).to.eq(201);
            expect(response.body.title).to.eq(testDoc.title);
            expect(response.body.content).to.eq(testDoc.content);
            createdId = response.body.id;
            cy.wrap(createdId).as('createdId');
        });

        // 2. Read (Get By ID)
        cy.get('@createdId').then((id) => {
            cy.request({
                method: 'GET',
                url: `/api/documents/${id}`,
                headers: { Authorization: `Bearer ${token}` }
            }).then((response) => {
                expect(response.status).to.eq(200);
                expect(response.body.title).to.eq(testDoc.title);
                expect(response.body.content).to.eq(testDoc.content);
            });
        });

        // 3a. Update Title Only
        cy.get('@createdId').then((id) => {
            cy.request({
                method: 'PUT',
                url: `/api/documents/${id}`,
                headers: { Authorization: `Bearer ${token}` },
                body: { title: 'Title Only Update', content: 'Preserved Content' }
            }).then((response) => {
                expect(response.status).to.eq(200, 'Update Title Only failed');
            });
        });

        // 3b. Update Content Only (Must include title)
        cy.get('@createdId').then((id) => {
            cy.request({
                method: 'PUT',
                url: `/api/documents/${id}`,
                headers: { Authorization: `Bearer ${token}` },
                body: { title: 'Title Only Update', content: 'Content Only Update' }
            }).then((response) => {
                expect(response.status).to.eq(200, 'Update Content Only failed');
            });
        });

        // 3c. Update Tags Only
        cy.get('@createdId').then((id) => {
            cy.request({
                method: 'PUT',
                url: `/api/documents/${id}`,
                headers: { Authorization: `Bearer ${token}` },
                body: { title: 'Title Only Update', content: 'Content Only Update', tags: [{ name: 'api-test-updated' }] }
            }).then((response) => {
                expect(response.status).to.eq(200, 'Update Tags Only failed');
            });
        });

        // 3d. Update Attachments Only (Empty list)
        cy.get('@createdId').then((id) => {
            cy.request({
                method: 'PUT',
                url: `/api/documents/${id}`,
                headers: { Authorization: `Bearer ${token}` },
                body: { title: 'Title Only Update', content: 'Content Only Update', attachments: [] }
            }).then((response) => {
                expect(response.status).to.eq(200, 'Update Attachments Empty failed');
            });
        });

        // 4. Verify in List (Check final state)
        cy.request({
            method: 'GET',
            url: '/api/documents',
            headers: { Authorization: `Bearer ${token}` }
        }).then((response) => {
            expect(response.status).to.eq(200);
            // Handle Pagination
            const docs = response.body.content || response.body;
            const found = docs.find(d => d.title === 'Title Only Update'); 
            // Note: Title persists from 3a. Content from 3b.
            expect(found, 'Document should exist').to.exist;
        });
    });
});
