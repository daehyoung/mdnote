package kr.luxsoft.mdnote.controller; // Placing in controller package to easily mock MVC or just use repository

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.DocumentRepository;
import kr.luxsoft.mdnote.repository.UserRepository;
import kr.luxsoft.mdnote.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // Uses H2 usually if configured, or default
public class PermissionsIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        // Clear DB
        documentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void adminShouldSeePrivateDocuments() {
        // 1. Create Normal User
        User normalUser = new User();
        normalUser.setUsername("user1");
        normalUser.setPasswordHash("pass");
        normalUser.setRole("USER");
        normalUser.setName("User 1");
        userRepository.save(normalUser);

        // 2. Create Private Document (Public Read = false)
        Document doc = new Document();
        doc.setTitle("Private Doc");
        doc.setContent("Content");
        doc.setAuthor(normalUser);
        doc.setPublicRead(false);
        doc.setGroupRead(false);
        documentRepository.save(doc);

        // 3. Create Admin User
        User adminUser = new User();
        adminUser.setUsername("admin1");
        adminUser.setPasswordHash("pass");
        adminUser.setRole("ADMIN"); 
        adminUser.setName("Admin User");
        userRepository.save(adminUser);

        // 4. Search as Admin
        Page<Document> result = documentService.getAllDocuments(
                null, null, null, PageRequest.of(0, 10), "admin1"
        );

        // 5. Verify
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Private Doc");
    }
    
    @Test
    public void userShouldNotSeePrivateDocuments() {
        // 1. Create Normal User 1 (Author)
        User author = new User();
        author.setUsername("user1");
        author.setPasswordHash("pass");
        author.setRole("USER"); 
        userRepository.save(author);

        // 2. Create Normal User 2 (Stranger)
        User stranger = new User();
        stranger.setUsername("user2");
        stranger.setPasswordHash("pass");
        stranger.setRole("USER");
        userRepository.save(stranger);
        
        // 3. Create Private Document
        Document doc = new Document();
        doc.setTitle("Private Doc");
        doc.setAuthor(author);
        doc.setPublicRead(false);
        doc.setGroupRead(false);
        documentRepository.save(doc);

        // 4. Search as Stranger
        Page<Document> result = documentService.getAllDocuments(
                null, null, null, PageRequest.of(0, 10), "user2"
        );

        // 5. Verify
        assertThat(result.getContent()).isEmpty();
    }
}
