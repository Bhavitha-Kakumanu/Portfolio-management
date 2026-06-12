package com.robinhood.user.dto;

import com.robinhood.user.model.User;

import java.time.Instant;
import java.util.UUID;

// What we expose to the outside world — never expose the password hash
public record UserResponse(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    String role,
    Instant createdAt
) {
    // Factory method: converts the JPA entity to a safe response DTO
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getCreatedAt()
        );
    }
}
