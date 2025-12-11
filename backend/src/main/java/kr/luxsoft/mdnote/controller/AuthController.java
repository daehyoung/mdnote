package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.dto.LoginRequest;
import kr.luxsoft.mdnote.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Todo: Implement actual authentication logic using AuthenticationManager
        return ResponseEntity.ok(new LoginResponse("dummy-token"));
    }
}
