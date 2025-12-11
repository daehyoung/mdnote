package com.example.mdnote.controller;

import com.example.mdnote.dto.LoginRequest;
import com.example.mdnote.dto.LoginResponse;
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
