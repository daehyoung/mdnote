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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Todo: Implement actual authentication logic using AuthenticationManager
        String theme = userRepository.findByUsername(loginRequest.getUsername())
                .map(kr.luxsoft.mdnote.model.User::getTheme)
                .orElse("light");
        
        log.info("Login attempt for user: {}, Found theme: {}", loginRequest.getUsername(), theme);
        
        return ResponseEntity.ok(new LoginResponse(loginRequest.getUsername(), theme));
    }
}
