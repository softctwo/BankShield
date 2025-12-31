package com.bankshield.auth.service;

import com.bankshield.auth.dto.LoginRequest;
import com.bankshield.auth.dto.LoginResponse;
import com.bankshield.auth.dto.RegisterRequest;
import com.bankshield.auth.dto.RegisterResponse;
import com.bankshield.auth.repository.UserRepository;
import com.bankshield.auth.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void testRegisterUser_UsernameExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFullName("Test User");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(request);
        });
    }
    
    @Test
    void testRegisterUser_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFullName("Test User");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(request);
        });
    }
    
    @Test
    void testVerifyToken_ValidToken_ReturnsTrue() {
        String token = "valid-token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        
        boolean result = authService.verifyToken(token);
        
        assertTrue(result);
    }
    
    @Test
    void testVerifyToken_InvalidToken_ReturnsFalse() {
        String token = "invalid-token";
        when(tokenProvider.validateToken(token)).thenReturn(false);
        
        boolean result = authService.verifyToken(token);
        
        assertFalse(result);
    }
}