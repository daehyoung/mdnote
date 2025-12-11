package com.example.mdnote.controller;

import com.example.mdnote.model.Attachment;
import com.example.mdnote.repository.AttachmentRepository;
import com.example.mdnote.service.FileStorageService;
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
public class AttachmentController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(@RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
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
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentRepository.deleteById(id);
        // Ideally also delete physical file using fileStorageService
        return ResponseEntity.ok().build();
    }
}
