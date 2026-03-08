package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.dto.DepartmentDTO;
import kr.luxsoft.mdnote.model.Department;
import kr.luxsoft.mdnote.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Organizational structure management APIs")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/tree")
    @Operation(summary = "Get Department Tree", description = "Fetches the full organizational hierarchy.")
    public ResponseEntity<List<DepartmentDTO>> getTree() {
        return ResponseEntity.ok(departmentService.getDepartmentTree());
    }

    @PostMapping
    @Operation(summary = "Create Department", description = "Adds a new department to the hierarchy.")
    public ResponseEntity<Department> create(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null ? ((Number) payload.get("parentId")).longValue() : null;
        return ResponseEntity.ok(departmentService.createDepartment(name, parentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Department", description = "Updates a department's name.")
    public ResponseEntity<Department> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, payload.get("name")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Department", description = "Removes a department from the system.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }
}
