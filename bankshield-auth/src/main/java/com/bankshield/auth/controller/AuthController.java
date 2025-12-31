package com.bankshield.auth.controller;

import com.bankshield.auth.dto.LoginRequest;
import com.bankshield.auth.dto.LoginResponse;
import com.bankshield.auth.dto.RegisterRequest;
import com.bankshield.auth.dto.RegisterResponse;
import com.bankshield.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, "Invalid credentials", false));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse response = authService.registerUser(registerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse(null, e.getMessage(), false));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new LoginResponse(null, "Logged out successfully", true));
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = authService.verifyToken(token.replace("Bearer ", ""));
            return ResponseEntity.ok(new LoginResponse(null, isValid ? "Token valid" : "Token invalid", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponse(null, "Token invalid", false));
        }
    }
}