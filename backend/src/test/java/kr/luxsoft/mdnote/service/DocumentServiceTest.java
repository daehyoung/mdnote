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
}
