package com.example.mdnote.controller;

import com.example.mdnote.model.Attachment;
import com.example.mdnote.repository.AttachmentRepository;
import com.example.mdnote.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttachmentControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentController attachmentController;

    @Test
    public void testUploadFile() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        String generatedFileName = "uuid_test.txt";
        
        when(fileStorageService.storeFile(any())).thenReturn(generatedFileName);
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(i -> {
            Attachment a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        ResponseEntity<Attachment> response = attachmentController.uploadFile(file);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(generatedFileName, response.getBody().getFileName());
        // Re-checking logic: 
        // Controller now does: attachment.setFileName(fileName); where fileName is UUID one.
        // So expectation should be generatedFileName
    }
}
