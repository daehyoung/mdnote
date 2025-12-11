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
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null ? ((Number) payload.get("parentId")).longValue() : null;
        return ResponseEntity.ok(categoryService.createCategory(name, parentId));
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
