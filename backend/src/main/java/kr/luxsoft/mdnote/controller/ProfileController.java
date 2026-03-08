package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.model.User;
import kr.luxsoft.mdnote.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile and personal settings APIs")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "Get Profile", description = "Fetches the current authenticated user's profile details.")
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
    @Operation(summary = "Update Profile", description = "Updates profile information (name, email, theme) for the current user.")
    public ResponseEntity<User> updateProfile(Principal principal, @RequestBody Map<String, String> payload) {
        String username = (principal != null) ? principal.getName() : "test";
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (payload.containsKey("name")) user.setName(payload.get("name"));
                    if (payload.containsKey("email")) user.setEmail(payload.get("email"));
                    if (payload.containsKey("theme")) user.setTheme(payload.get("theme"));
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/password")
    @Operation(summary = "Update Password", description = "Changes the password for the current user.")
    public ResponseEntity<Void> updatePassword(Principal principal, @RequestBody Map<String, String> payload) {
        String username = (principal != null) ? principal.getName() : "test";
        String newPassword = payload.get("password");
        if (newPassword == null || newPassword.isEmpty()) return ResponseEntity.badRequest().build();

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
