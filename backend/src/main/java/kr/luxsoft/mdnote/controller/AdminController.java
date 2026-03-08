package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin") 
@Slf4j
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private kr.luxsoft.mdnote.repository.DepartmentRepository departmentRepository;

    @Autowired
    private kr.luxsoft.mdnote.service.UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
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
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.debug("AdminController: Creating user {}", user.getUsername());
        // Default values
        if (user.getStatus() == null) user.setStatus("ACTIVE");
        if (user.getRole() == null) user.setRole("USER");
        // Hash password (assuming provided raw in passwordHash for creation)
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}
