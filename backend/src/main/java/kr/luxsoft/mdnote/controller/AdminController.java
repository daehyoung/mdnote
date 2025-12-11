package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
// @PreAuthorize("hasRole('ADMIN')") // In our basic setup, we might rely on Frontend hiding or filter.
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private kr.luxsoft.mdnote.repository.DepartmentRepository departmentRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestBody String status) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(status);
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/department")
    public ResponseEntity<User> updateUserDepartment(@PathVariable Long id, @RequestBody Long deptId) {
        return userRepository.findById(id)
                .map(user -> {
                    if (deptId == null) {
                        user.setDepartment(null);
                    } else {
                        kr.luxsoft.mdnote.model.Department dept = departmentRepository.findById(deptId).orElse(null);
                        user.setDepartment(dept);
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
