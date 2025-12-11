package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Document;
import kr.luxsoft.mdnote.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam("q") String query) {
        return ResponseEntity.ok(documentService.searchDocuments(query));
    }
}
