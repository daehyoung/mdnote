package com.example.mdnote.controller;

import com.example.mdnote.model.Comment;
import com.example.mdnote.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{docId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long docId) {
        return ResponseEntity.ok(commentService.getComments(docId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long docId,
                                              @RequestBody Map<String, String> payload,
                                              Authentication authentication) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // In real auth, get username from authentication principal
        // For dummy token, we might not have a real principal if we didn't set it up in filter.
        // But our SecurityConfig is basic.
        // Let's assume we can get name, or default to "admin" if null for now during dev
        String username = authentication != null ? authentication.getName() : "admin";
        
        return ResponseEntity.ok(commentService.addComment(docId, username, content));
    }
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long docId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
