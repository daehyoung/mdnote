package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Attachment;
import kr.luxsoft.mdnote.repository.AttachmentRepository;
import kr.luxsoft.mdnote.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/attachments")
@Tag(name = "Attachments", description = "File upload and download APIs")
public class AttachmentController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @PostMapping("/upload")
    @Operation(summary = "Upload File", description = "Uploads an attachment and returns its metadata.")
    public ResponseEntity<Attachment> uploadFile(@Parameter(description = "Multipart file to upload", required = true) @RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = fileStorageService.storeFile(file); // Stores as UUID+Ext
        
        Attachment attachment = new Attachment();
        attachment.setFileName(fileName); // Physical Name (UUID)
        attachment.setOriginalFileName(originalFileName); // Logical Name
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(""); 

        Attachment saved = attachmentRepository.save(attachment);
        
        // Update URI with actual ID
        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/attachments/")
                .path(saved.getId().toString())
                .toUriString();
        saved.setFilePath(uri);
        attachmentRepository.save(saved);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download File", description = "Downloads an attachment by ID.")
    public ResponseEntity<Resource> downloadFile(@Parameter(description = "Attachment ID", required = true) @PathVariable Long id, HttpServletRequest request) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found " + id));
                
        // Load file using physical name
        Resource resource = fileStorageService.loadFileAsResource(attachment.getFileName());

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // logger.info("Could not determine file type.");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        
        // Use logical name for download
        // Ensure strictly ASCII or encoded if needed, but for simplicity assuming safe names or browser handles UTF-8
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(resource);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Attachment", description = "Removes an attachment metadata.")
    public ResponseEntity<Void> deleteAttachment(@Parameter(description = "Attachment ID to delete", required = true) @PathVariable Long id) {
        attachmentRepository.deleteById(id);
        // Ideally also delete physical file using fileStorageService
        return ResponseEntity.ok().build();
    }
}
