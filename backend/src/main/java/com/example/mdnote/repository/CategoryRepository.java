package com.example.mdnote.repository;

import com.example.mdnote.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull(); // Roots
    List<Category> findAll();
}
