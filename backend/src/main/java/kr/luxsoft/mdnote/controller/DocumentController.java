package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public Page<Document> getAllDocuments(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tagName, // Added tagName
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String[] sort,
            java.security.Principal principal) {
        
        // Parse sort
        String sortField = "updatedAt";
        org.springframework.data.domain.Sort.Direction direction = org.springframework.data.domain.Sort.Direction.DESC;
        
        if (sort.length > 0) {
           if (sort[0].contains(",")) {
               // support "property,asc" format from spring data defaults
                String[] parts = sort[0].split(",");
                sortField = parts[0];
                if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) direction = org.springframework.data.domain.Sort.Direction.ASC;
           } else {
               sortField = sort[0];
               if (sort.length > 1 && "asc".equalsIgnoreCase(sort[1])) direction = org.springframework.data.domain.Sort.Direction.ASC;
           }
        }
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortField));
        String callbackUsername = (principal != null) ? principal.getName() : null;
        System.out.println("DEBUG: DocumentController.getAllDocuments called");
        System.out.println("DEBUG params: categoryId=" + categoryId + ", status=" + status + ", tagName=" + tagName + ", query=" + query);
        return documentService.getAllDocuments(categoryId, status, tagName, query, pageable, callbackUsername);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document document, java.security.Principal principal) {
        Document created = documentService.createDocument(document, principal.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document document, java.security.Principal principal) {
        try {
            Document updated = documentService.updateDocument(id, document, principal.getName());
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, java.security.Principal principal) {
        try {
            documentService.deleteDocument(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
