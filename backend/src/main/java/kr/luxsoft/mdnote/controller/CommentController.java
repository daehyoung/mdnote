package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.Comment;
import kr.luxsoft.mdnote.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{docId}/comments")
@Tag(name = "Comments", description = "Document commenting and discussion APIs")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    @Operation(summary = "Get Document Comments", description = "Fetches all comments for a specific document.")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long docId) {
        return ResponseEntity.ok(commentService.getComments(docId));
    }

    @PostMapping
    @Operation(summary = "Add Comment", description = "Posts a new comment to a document.")
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
    @Operation(summary = "Delete Comment", description = "Removes a comment by ID.")
    public ResponseEntity<Void> deleteComment(@PathVariable Long docId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
