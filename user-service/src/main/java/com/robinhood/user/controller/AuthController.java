package com.robinhood.user.controller;

import com.robinhood.user.dto.AuthResponse;
import com.robinhood.user.dto.LoginRequest;
import com.robinhood.user.dto.RegisterRequest;
import com.robinhood.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Public endpoints — no JWT required (configured in SecurityConfig)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/v1/auth/register
    // @Valid triggers the @NotBlank, @Email etc. constraints on RegisterRequest.
    // If validation fails, Spring throws MethodArgumentNotValidException → 400.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}
