package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.dto.LoginRequest;
import kr.luxsoft.mdnote.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@lombok.extern.slf4j.Slf4j
public class AuthController {

    @Autowired
    private kr.luxsoft.mdnote.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private kr.luxsoft.mdnote.security.JwtTokenProvider jwtTokenProvider;

    @org.springframework.beans.factory.annotation.Value("${app.auth.type:simple}")
    private String authType;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return userRepository.findByUsername(loginRequest.getUsername())
            .map(user -> {
                if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                    String token;
                    if ("jwt".equalsIgnoreCase(authType)) {
                        token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
                    } else {
                        token = user.getUsername(); // Simple mode
                    }
                    
                    log.info("Login success: {}, type: {}", user.getUsername(), authType);
                    return ResponseEntity.ok(new LoginResponse(token, user.getTheme()));
                } else {
                    log.warn("Login failed: invalid password for {}", loginRequest.getUsername());
                    return ResponseEntity.status(401).body(new LoginResponse(null, null));
                }
            })
            .orElseGet(() -> {
                log.warn("Login failed: user not found {}", loginRequest.getUsername());
                return ResponseEntity.status(401).body(new LoginResponse(null, null));
            });
    }
}
