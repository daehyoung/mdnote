package com.example.mdnote.controller;

import com.example.mdnote.dto.DepartmentDTO;
import com.example.mdnote.model.Department;
import com.example.mdnote.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/tree")
    public ResponseEntity<List<DepartmentDTO>> getTree() {
        return ResponseEntity.ok(departmentService.getDepartmentTree());
    }

    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null ? ((Number) payload.get("parentId")).longValue() : null;
        return ResponseEntity.ok(departmentService.createDepartment(name, parentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, payload.get("name")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }
}
