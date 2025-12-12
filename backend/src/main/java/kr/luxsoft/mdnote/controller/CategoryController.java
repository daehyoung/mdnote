package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.dto.CategoryDTO;
import kr.luxsoft.mdnote.model.Category;
import kr.luxsoft.mdnote.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public ResponseEntity<Map<String, List<CategoryDTO>>> getCategoryTree(java.security.Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(categoryService.getCategoryTree(username));
    }

    @PostMapping
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
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(categoryService.updateCategory(id, payload.get("name")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
