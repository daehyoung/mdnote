package com.example.mdnote.controller;

import com.example.mdnote.model.User;
import com.example.mdnote.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<User> getProfile(Principal principal) {
        // In our simple mock auth, Principal might be null or just a name. 
        // We need to fetch by username from context or simulate it.
        // Assuming @WithMockUser or proper auth sets Principal.
        // If not, we might need to pass username. 
        // For this demo with 'test' user, we can try to look up 'test' if principal is null?
        // But better: Frontend sends user id or username? RESTful usually /me.
        
        String username = (principal != null) ? principal.getName() : "test"; // Fallback for dev
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(Principal principal, @RequestBody Map<String, String> payload) {
        String username = (principal != null) ? principal.getName() : "test";
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (payload.containsKey("name")) user.setName(payload.get("name"));
                    if (payload.containsKey("email")) user.setEmail(payload.get("email"));
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(Principal principal, @RequestBody Map<String, String> payload) {
        String username = (principal != null) ? principal.getName() : "test";
        String newPassword = payload.get("password");
        if (newPassword == null || newPassword.isEmpty()) return ResponseEntity.badRequest().build();

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
