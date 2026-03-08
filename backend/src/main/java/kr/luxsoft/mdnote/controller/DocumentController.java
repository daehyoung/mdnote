package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document management, search, and filtering APIs")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    @Operation(summary = "Get All Documents", description = "Fetches a paged list of documents with optional filtering by category, status, tag, or keyword.")
    public Page<Document> getAllDocuments(
            @Parameter(description = "Category ID to filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Document status (e.g., ACTIVE, ARCHIVED)") @RequestParam(required = false) String status,
            @Parameter(description = "Tag name to filter") @RequestParam(required = false) String tagName,
            @Parameter(description = "Search keyword in title/content") @RequestParam(required = false) String query,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort criteria (e.g., updatedAt,desc)") @RequestParam(defaultValue = "updatedAt,desc") String[] sort,
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

    @GetMapping("/search")
    @Operation(summary = "Search Documents", description = "Specifically searches for documents by keyword with full pagination.")
    public Page<Document> searchDocuments(
            @Parameter(description = "Search query keyword", required = true) @RequestParam String query,
            @Parameter(description = "Page index (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort criteria (e.g., title,asc)") @RequestParam(defaultValue = "updatedAt,desc") String[] sort) {
        
        // Similar sort parsing logic as getAllDocuments
        String sortField = "updatedAt";
        org.springframework.data.domain.Sort.Direction direction = org.springframework.data.domain.Sort.Direction.DESC;
        
        if (sort.length > 0) {
            if (sort[0].contains(",")) {
                String[] parts = sort[0].split(",");
                sortField = parts[0];
                if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) direction = org.springframework.data.domain.Sort.Direction.ASC;
            } else {
                sortField = sort[0];
                if (sort.length > 1 && "asc".equalsIgnoreCase(sort[1])) direction = org.springframework.data.domain.Sort.Direction.ASC;
            }
        }
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortField));
        return documentService.searchDocuments(query, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Document Details", description = "Returns detail information for a single document by ID.")
    public ResponseEntity<Document> getDocumentById(@Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create Document", description = "Creates a new document using the provided JSON body.")
    public ResponseEntity<Document> createDocument(@RequestBody Document document, java.security.Principal principal) {
        Document created = documentService.createDocument(document, principal.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Document", description = "Updates an existing document. Only accessible by the owner or admin.")
    public ResponseEntity<Document> updateDocument(@Parameter(description = "Document ID to update") @PathVariable Long id, @RequestBody Document document, java.security.Principal principal) {
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
    @Operation(summary = "Delete Document", description = "Removes a document from the system. Only accessible by the owner or admin.")
    public ResponseEntity<Void> deleteDocument(@Parameter(description = "Document ID to delete") @PathVariable Long id, java.security.Principal principal) {
        try {
            documentService.deleteDocument(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
