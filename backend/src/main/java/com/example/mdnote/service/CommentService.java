package com.example.mdnote.service;

import com.example.mdnote.model.Comment;
import com.example.mdnote.model.Document;
import com.example.mdnote.model.User;
import com.example.mdnote.repository.CommentRepository;
import com.example.mdnote.repository.DocumentRepository;
import com.example.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository; // To fetch user by username

    public Comment addComment(Long docId, String username, String content) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setDocument(doc);
        comment.setUser(user);
        comment.setContent(content);
        
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long docId) {
        return commentRepository.findByDocumentId(docId);
    }
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
