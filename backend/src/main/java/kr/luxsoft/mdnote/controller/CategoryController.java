package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.dto.CategoryDTO;
import kr.luxsoft.mdnote.model.Category;
import kr.luxsoft.mdnote.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Hierarchical category management for documents")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "Get Category Tree", description = "Fetches a hierarchical tree of categories, including system-wide and user-specific roots.")
    public ResponseEntity<Map<String, List<CategoryDTO>>> getCategoryTree(java.security.Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(categoryService.getCategoryTree(username));
    }

    @PostMapping
    @Operation(summary = "Create Category", description = "Creates a new category under a parent or as a root.")
    public ResponseEntity<Category> create(@RequestBody Map<String, Object> payload, java.security.Principal principal) {
        String name = (String) payload.get("name");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null ? ((Number) payload.get("parentId")).longValue() : null;
        String scope = (String) payload.getOrDefault("scope", "SYSTEM"); 
        
        // If client doesn't send scope but has parentId, scope is inferred in service, but we pass it anyway.
        // If client sends "USER" and has no parent, it's a User Root.
        
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(categoryService.createCategory(name, parentId, username, scope));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Category", description = "Updates a category's name.")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(categoryService.updateCategory(id, payload.get("name")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category", description = "Removes a category and its associations.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
