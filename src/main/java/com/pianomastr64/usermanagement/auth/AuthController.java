package com.pianomastr64.usermanagement.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService service;
    
    public AuthController(AuthService service) {
        this.service = service;
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Validated AuthRequest request) {
        String token = service.authenticate(request);
        return ResponseEntity.ok(Map.of("token", token));
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Validated RegisterRequest request) {
        String token = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("token", token));
    }
}
