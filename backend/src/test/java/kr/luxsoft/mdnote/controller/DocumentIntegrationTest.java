package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Attachment;
import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.model.Tag;
import kr.luxsoft.mdnote.repository.AttachmentRepository;
import kr.luxsoft.mdnote.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private kr.luxsoft.mdnote.repository.UserRepository userRepository;

    @BeforeEach
    public void setup() {
        // Safe test: Do not wipe DB
        // documentRepository.deleteAll();
        // attachmentRepository.deleteAll();
        
        // Ensure testuser exists for DocumentService lookup
        if (userRepository.findByUsername("testuser").isEmpty()) {
            kr.luxsoft.mdnote.model.User user = new kr.luxsoft.mdnote.model.User();
            user.setUsername("testuser");
            user.setPasswordHash("dummy"); // not used by mock user
            userRepository.save(user);
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testFullDocumentLifecycleWithAttachment() throws Exception {
        // 1. Upload a File
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "real_test.txt", "text/plain", "Hello Real Attachment World!".getBytes());

        String attachResponse = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Attachment uploadedAtt = objectMapper.readValue(attachResponse, Attachment.class);
        Long attId = uploadedAtt.getId();

        // 2. Create a Document
        Document doc = new Document();
        doc.setTitle("Real Test Doc");
        doc.setContent("Initial");
        
        String createResponse = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Document createdDoc = objectMapper.readValue(createResponse, Document.class);
        Long docId = createdDoc.getId();

        // 3. Update Document with New Content and Link Attachment
        createdDoc.setContent("Updated Real Content");
        List<Attachment> attachments = new ArrayList<>();
        attachments.add(uploadedAtt);
        createdDoc.setAttachments(attachments);

        mockMvc.perform(put("/api/documents/" + docId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdDoc)))
                .andExpect(status().isOk());

        // 4. Verify Document Content and Attachment Link
        mockMvc.perform(get("/api/documents/" + docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Real Content"))
                .andExpect(jsonPath("$.attachments[0].id").value(attId))
                .andExpect(jsonPath("$.attachments[0].fileName").value(uploadedAtt.getFileName()));

        mockMvc.perform(get("/api/attachments/" + attId))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Real Attachment World!"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAdminAccessToPrivateDoc() throws Exception {
        // Setup: Create a private document as "user1"
        // Since we are mocking user as "admin", we need to manually create doc via repository or Service
        
        // 1. Ensure user1 exists
        kr.luxsoft.mdnote.model.User user1;
        if (userRepository.findByUsername("user1").isEmpty()) {
            user1 = new kr.luxsoft.mdnote.model.User();
            user1.setUsername("user1");
            user1.setPasswordHash("pass");
            user1.setRole("USER");
            userRepository.save(user1);
        } else {
             user1 = userRepository.findByUsername("user1").get();
        }
        
        // 2. Create Private Doc
        Document privateDoc = new Document();
        privateDoc.setTitle("Private Admin Test");
        privateDoc.setContent("Secret");
        privateDoc.setAuthor(user1);
        privateDoc.setPublicRead(false);
        privateDoc.setGroupRead(false);
        documentRepository.save(privateDoc);
        
        // 3. Search as Admin (MockUser is Admin)
        // Ensure Admin exists in DB for Service check (Service looks up findByUsername)
        if (userRepository.findByUsername("admin").isEmpty()) {
             kr.luxsoft.mdnote.model.User admin = new kr.luxsoft.mdnote.model.User();
             admin.setUsername("admin");
             admin.setPasswordHash("pass");
             admin.setRole("ADMIN");
             userRepository.save(admin);
        }

        mockMvc.perform(get("/api/documents")
                .param("query", "Private Admin Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Private Admin Test"));
        
        // 4. Cleanup
        documentRepository.delete(privateDoc);
    }
}
