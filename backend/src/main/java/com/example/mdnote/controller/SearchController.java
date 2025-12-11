package com.example.mdnote.controller;

import com.example.mdnote.model.Document;
import com.example.mdnote.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class SearchController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam("q") String query) {
        return ResponseEntity.ok(documentService.searchDocuments(query));
    }
}
