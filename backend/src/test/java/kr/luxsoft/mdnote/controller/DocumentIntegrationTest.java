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

    @BeforeEach
    public void setup() {
        // Safe test: Do not wipe DB
        // documentRepository.deleteAll();
        // attachmentRepository.deleteAll();
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

        // 5. Verify Attachment Content (Download)
        mockMvc.perform(get("/api/attachments/" + attId))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Real Attachment World!"));
    }
}
