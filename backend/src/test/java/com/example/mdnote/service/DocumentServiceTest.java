package com.example.mdnote.service;

import com.example.mdnote.model.Document;
import com.example.mdnote.repository.DocumentRepository;
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

    @InjectMocks
    private DocumentService documentService;

    @Test
    public void testCreateDocument() {
        Document doc = new Document();
        doc.setTitle("Test Doc");
        doc.setContent("Content");

        when(documentRepository.save(any(Document.class))).thenReturn(doc);

        Document created = documentService.createDocument(doc);

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
