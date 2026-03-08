package kr.luxsoft.mdnote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Administration", description = "User management and administrative controls")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private kr.luxsoft.mdnote.repository.DepartmentRepository departmentRepository;

    @Autowired
    private kr.luxsoft.mdnote.service.UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Fetches a paged list of all users in the system.")
    public ResponseEntity<Page<User>> getAllUsers(@Parameter(description = "Pagination and sorting criteria") Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Update User Status", description = "Updates the status (e.g., ACTIVE, INACTIVE) of a user.")
    public ResponseEntity<User> updateUserStatus(@Parameter(description = "User ID", required = true) @PathVariable Long id, @RequestBody String status) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(status);
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/department")
    @Operation(summary = "Update User Department", description = "Assigns or removes a department for a user.")
    public ResponseEntity<User> updateUserDepartment(@Parameter(description = "User ID", required = true) @PathVariable Long id, @Parameter(description = "Department ID (null to remove)") @RequestBody Long deptId) {
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
    @Operation(summary = "Create User", description = "Registers a new user with base settings.")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.debug("AdminController: Creating user {}", user.getUsername());
        // Default values
        if (user.getStatus() == null) user.setStatus("ACTIVE");
        if (user.getRole() == null) user.setRole("USER");
        // Hash password (assuming provided raw in passwordHash for creation)
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete User", description = "Permanently removes a user from the system.")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID to delete", required = true) @PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update User Info", description = "Updates a user's general profile information.")
    public ResponseEntity<User> updateUser(@Parameter(description = "User ID to update", required = true) @PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/users/{id}/password")
    @Operation(summary = "Reset User Password", description = "Administratively resets a user's password.")
    public ResponseEntity<?> resetPassword(@Parameter(description = "User ID", required = true) @PathVariable Long id, @RequestBody String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}
