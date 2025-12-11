package com.example.mdnote.controller;

import com.example.mdnote.model.Comment;
import com.example.mdnote.model.User;
import com.example.mdnote.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    @WithMockUser
    public void testGetComments() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test Comment");
        comment.setCreatedAt(LocalDateTime.now());
        User user = new User();
        user.setName("John Doe");
        comment.setUser(user);

        when(commentService.getComments(anyLong())).thenReturn(Arrays.asList(comment));

        mockMvc.perform(get("/api/documents/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test Comment"))
                .andExpect(jsonPath("$[0].user.name").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "admin")
    public void testAddComment() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("New Comment");

        when(commentService.addComment(anyLong(), anyString(), anyString())).thenReturn(comment);

        Map<String, String> payload = new HashMap<>();
        payload.put("content", "New Comment");

        mockMvc.perform(post("/api/documents/1/comments")
                        .with(csrf()) // Since we have spring security (even if basic), csrf might be needed or ignored depending on config.
                        // Actually our config disables csrf, but WebMvcTest might load default security?
                        // We provided SecurityConfig in main, but WebMvcTest focuses on Controller.
                        // Usually better to include SecurityConfig or just Mock csrf.
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("New Comment"));
    }
}
