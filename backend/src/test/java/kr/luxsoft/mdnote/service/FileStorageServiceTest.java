package kr.luxsoft.mdnote.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private final String testUploadDir = "test-uploads";

    @BeforeEach
    public void setup() throws IOException {
        fileStorageService = new FileStorageService(testUploadDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Path path = Paths.get(testUploadDir);
        if (Files.exists(path)) {
            FileSystemUtils.deleteRecursively(path);
        }
    }

    @Test
    public void testStoreAndLoadFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes());

        String fileName = fileStorageService.storeFile(file);
        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".txt"));

        Resource resource = fileStorageService.loadFileAsResource(fileName);
        assertTrue(resource.exists());
        assertEquals("Hello World", new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    public void testStoreFileWithTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../traversal.txt", "text/plain", "Danger".getBytes());

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    public void testFileNotFound() {
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadFileAsResource("non-existent.txt");
        });
    }
}
