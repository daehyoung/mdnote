package kr.luxsoft.mdnote.service;

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private kr.luxsoft.mdnote.repository.UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    public void testCreateDocument() {
        Document doc = new Document();
        doc.setTitle("Test Doc");
        doc.setContent("Content");
        
        kr.luxsoft.mdnote.model.User user = new kr.luxsoft.mdnote.model.User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(documentRepository.save(any(Document.class))).thenReturn(doc);

        Document created = documentService.createDocument(doc, "testuser");

        assertEquals("Test Doc", created.getTitle());
        verify(documentRepository).save(doc);
    }

    @Test
    public void testGetDocumentById() {
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle("Test Doc");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        Optional<Document> found = documentService.getDocumentById(1L);

        assertTrue(found.isPresent());
        assertEquals("Test Doc", found.get().getTitle());
    }

    @Test
    public void testUpdateDocumentAsAuthor() {
        kr.luxsoft.mdnote.model.User author = new kr.luxsoft.mdnote.model.User();
        author.setUsername("author");
        
        Document existing = new Document();
        existing.setId(1L);
        existing.setAuthor(author);
        existing.setTitle("Old Title");

        Document details = new Document();
        details.setTitle("New Title");

        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document updated = documentService.updateDocument(1L, details, "author");

        assertNotNull(updated);
        assertEquals("New Title", updated.getTitle());
    }

    @Test
    public void testUpdateDocumentDenied() {
        kr.luxsoft.mdnote.model.User author = new kr.luxsoft.mdnote.model.User();
        author.setUsername("author");
        
        kr.luxsoft.mdnote.model.User stranger = new kr.luxsoft.mdnote.model.User();
        stranger.setUsername("stranger");

        Document existing = new Document();
        existing.setId(1L);
        existing.setAuthor(author);
        existing.setPublicWrite(false);
        existing.setGroupWrite(false);

        Document details = new Document();
        details.setTitle("Hacked Title");

        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            documentService.updateDocument(1L, details, "stranger");
        });
    }

    @Test
    public void testUpdateDocumentGroupPermission() {
        kr.luxsoft.mdnote.model.Department dept = new kr.luxsoft.mdnote.model.Department();
        dept.setId(10L);

        kr.luxsoft.mdnote.model.User author = new kr.luxsoft.mdnote.model.User();
        author.setUsername("author");
        author.setDepartment(dept);
        
        kr.luxsoft.mdnote.model.User colleague = new kr.luxsoft.mdnote.model.User();
        colleague.setUsername("colleague");
        colleague.setDepartment(dept);

        Document existing = new Document();
        existing.setId(1L);
        existing.setAuthor(author);
        existing.setGroupWrite(true); // Group permission enabled
        existing.setTitle("Initial Title");

        Document details = new Document();
        details.setTitle("Updated by Colleague");

        when(userRepository.findByUsername("colleague")).thenReturn(Optional.of(colleague));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document updated = documentService.updateDocument(1L, details, "colleague");

        assertNotNull(updated);
        assertEquals("Updated by Colleague", updated.getTitle());
    }

    @Test
    public void testDeleteDocumentAsAdmin() {
        kr.luxsoft.mdnote.model.User admin = new kr.luxsoft.mdnote.model.User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        Document doc = new Document();
        doc.setId(1L);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L, "admin");

        verify(documentRepository).deleteById(1L);
    }
}
