package kr.luxsoft.mdnote.service;

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.repository.DocumentRepository;
import kr.luxsoft.mdnote.repository.TagRepository;
import kr.luxsoft.mdnote.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private kr.luxsoft.mdnote.repository.UserRepository userRepository;

    public Page<Document> getAllDocuments(Long categoryId, String status, String query, Pageable pageable, String username) {
        kr.luxsoft.mdnote.model.User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        
        if (isAdmin) {
            return documentRepository.findAllAdmin(categoryId, status, query, pageable);
        }

        Long deptId = (currentUser.getDepartment() != null) ? currentUser.getDepartment().getId() : -1L;

        return documentRepository.findAllWithPermissions(categoryId, status, query, username, deptId, false, pageable);
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public Document createDocument(Document document, String username) {
        kr.luxsoft.mdnote.model.User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        document.setAuthor(user);
        
        processTags(document);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }
    
    public Document updateDocument(Long id, Document documentDetails, String username) {
        kr.luxsoft.mdnote.model.User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return documentRepository.findById(id).map(document -> {
            checkWritePermission(document, currentUser);

            document.setTitle(documentDetails.getTitle());
            document.setContent(documentDetails.getContent());
            document.setCategory(documentDetails.getCategory());
            document.setStatus(documentDetails.getStatus());
            document.setUpdatedAt(LocalDateTime.now());
            
            // Permission Updates
            // Only Owner or Admin can change permissions? 
            // Or allow anyone with Write access? Typically changing permissions is an Owner action.
            if ("ADMIN".equals(currentUser.getRole()) || 
               (document.getAuthor() != null && document.getAuthor().getUsername().equals(username))) {
                document.setGroupRead(documentDetails.isGroupRead());
                document.setGroupWrite(documentDetails.isGroupWrite());
                document.setPublicRead(documentDetails.isPublicRead());
                document.setPublicWrite(documentDetails.isPublicWrite());
                document.setAllowComments(documentDetails.isAllowComments());
            }
            
            // Tags
            if (documentDetails.getTags() != null) {
                processTags(documentDetails); // Ensure tags exist
                document.setTags(documentDetails.getTags());
            }

            // Attachments
            if (documentDetails.getAttachments() != null) {
                List<kr.luxsoft.mdnote.model.Attachment> atts = documentDetails.getAttachments();
                atts.forEach(a -> a.setDocument(document));
                
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

    public void deleteDocument(Long id, String username) {
        kr.luxsoft.mdnote.model.User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
            
        checkDeletePermission(document, currentUser);
        
        documentRepository.deleteById(id);
    }

    private void checkWritePermission(Document document, kr.luxsoft.mdnote.model.User user) {
        if ("ADMIN".equals(user.getRole())) return;
        if (document.getAuthor() != null && document.getAuthor().getUsername().equals(user.getUsername())) return;
        
        // Group Write
        if (document.isGroupWrite() && inSameDepartment(document.getAuthor(), user)) return;
        
        // Public Write
        if (document.isPublicWrite()) return;

        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this document");
    }

    private void checkDeletePermission(Document document, kr.luxsoft.mdnote.model.User user) {
        if ("ADMIN".equals(user.getRole())) return;
        if (document.getAuthor() != null && document.getAuthor().getUsername().equals(user.getUsername())) return;

        // Group Delete (Mapped to Write)
        if (document.isGroupWrite() && inSameDepartment(document.getAuthor(), user)) return;

        // Public Delete (Mapped to Write)
        if (document.isPublicWrite()) return;

        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this document");
    }

    private boolean inSameDepartment(kr.luxsoft.mdnote.model.User author, kr.luxsoft.mdnote.model.User user) {
        if (author == null || author.getDepartment() == null || user.getDepartment() == null) return false;
        return author.getDepartment().getId().equals(user.getDepartment().getId());
    }

    public List<Document> searchDocuments(String query) {
        return documentRepository.searchDocuments(query);
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
