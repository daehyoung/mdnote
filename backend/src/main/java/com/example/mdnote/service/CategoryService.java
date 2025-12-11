package com.example.mdnote.service;

import com.example.mdnote.dto.CategoryDTO;
import com.example.mdnote.model.Category;
import com.example.mdnote.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, CategoryDTO> dtoMap = new HashMap<>();
        List<CategoryDTO> roots = new ArrayList<>();

        // Create DTOs
        for (Category cat : allCategories) {
            dtoMap.put(cat.getId(), new CategoryDTO(cat.getId(), cat.getName()));
        }

        // Build Tree
        for (Category cat : allCategories) {
            CategoryDTO dto = dtoMap.get(cat.getId());
            if (cat.getParent() == null) {
                roots.add(dto);
            } else {
                CategoryDTO parentDTO = dtoMap.get(cat.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    public Category createCategory(String name, Long parentId) {
        Category category = new Category();
        category.setName(name);
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId).orElse(null);
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, String name) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(name);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        // Handle constraint violation if has children? 
        // For now, let JPA/DB handle it or cascade.
        categoryRepository.deleteById(id);
    }
}
