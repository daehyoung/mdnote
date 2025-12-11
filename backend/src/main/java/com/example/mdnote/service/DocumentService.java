package com.example.mdnote.service;

import com.example.mdnote.model.Document;
import com.example.mdnote.repository.DocumentRepository;
import com.example.mdnote.repository.TagRepository;
import com.example.mdnote.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TagRepository tagRepository;

    public List<Document> getAllDocuments(Long categoryId) {
        if (categoryId != null) {
            return documentRepository.findByCategoryId(categoryId);
        }
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public Document createDocument(Document document) {
        processTags(document);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }
    
    public Document updateDocument(Long id, Document documentDetails) {
        return documentRepository.findById(id).map(document -> {
            document.setTitle(documentDetails.getTitle());
            document.setContent(documentDetails.getContent());
            document.setCategory(documentDetails.getCategory());
            document.setStatus(documentDetails.getStatus());
            document.setUpdatedAt(LocalDateTime.now());
            
            // Tags
            if (documentDetails.getTags() != null) {
                processTags(documentDetails); // Ensure tags exist
                document.setTags(documentDetails.getTags());
            }

            // Attachments
            if (documentDetails.getAttachments() != null) {
                List<com.example.mdnote.model.Attachment> atts = documentDetails.getAttachments();
                atts.forEach(a -> a.setDocument(document));
                
                // Hibernate tip: Do not replace the collection reference if orphanRemoval is on.
                // Instead, clear and addAll.
                if (document.getAttachments() == null) {
                    document.setAttachments(atts);
                } else {
                    document.getAttachments().clear();
                    document.getAttachments().addAll(atts);
                }
            }

            return documentRepository.save(document);
        }).orElse(null);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    public List<Document> searchDocuments(String query) {
        return documentRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
    }
    
    private void processTags(Document doc) {
        if (doc.getTags() != null && !doc.getTags().isEmpty()) {
            Set<Tag> processedTags = new HashSet<>();
            for (Tag tag : doc.getTags()) {
                if (tag.getId() != null) {
                    // Existing tag by ID
                    tagRepository.findById(tag.getId()).ifPresent(processedTags::add);
                } else if (tag.getName() != null && !tag.getName().isEmpty()) {
                    // Find by name or create
                    Tag existing = tagRepository.findByName(tag.getName())
                        .orElseGet(() -> tagRepository.save(new Tag(tag.getName())));
                    processedTags.add(existing);
                }
            }
            doc.setTags(processedTags);
        }
    }
}
