package com.example.mdnote.repository;

import com.example.mdnote.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);
    List<Document> findByCategoryId(Long categoryId);
}
