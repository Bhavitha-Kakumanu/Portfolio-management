package com.robinhood.user.service;

import com.robinhood.user.dto.*;
import com.robinhood.user.exception.UserAlreadyExistsException;
import com.robinhood.user.exception.UserNotFoundException;
import com.robinhood.user.model.User;
import com.robinhood.user.repository.UserRepository;
import com.robinhood.user.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// @Service marks this as a Spring bean in the service layer.
// All business logic lives here — the controller just delegates.
@Service
@Transactional  // every public method runs in a DB transaction by default
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // Fail fast with a clear error instead of letting the DB throw a constraint violation
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());

        return new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndEnabled(request.email(), true)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // BCrypt compares the incoming plaintext against the stored hash
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    @Transactional(readOnly = true)  // readOnly = true is a hint to the DB: no writes, can optimize
    public UserResponse getUser(UUID id) {
        return userRepository.findById(id)
            .map(UserResponse::from)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    public UserResponse updateName(UUID id, String firstName, String lastName) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // No explicit save() needed — JPA detects the change and flushes at transaction end
        return UserResponse.from(user);
    }
}
