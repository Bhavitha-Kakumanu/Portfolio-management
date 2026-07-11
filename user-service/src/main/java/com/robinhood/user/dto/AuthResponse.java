package com.robinhood.user.dto;

import java.util.UUID;

// Returned after successful login or registration
public record AuthResponse(
    String token,       // JWT — the client stores this and sends it on every request
    String tokenType,   // always "Bearer"
    UUID userId,
    String username,
    String email
) {
    // Convenience constructor — tokenType is always "Bearer"
    public AuthResponse(String token, UUID userId, String username, String email) {
        this(token, "Bearer", userId, username, email);
    }
}
