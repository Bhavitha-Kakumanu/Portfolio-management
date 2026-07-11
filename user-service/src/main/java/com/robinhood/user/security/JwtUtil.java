package com.robinhood.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// JWT (JSON Web Token) structure: header.payload.signature
//
// header:  { "alg": "HS256", "typ": "JWT" }
// payload: { "sub": "user-uuid", "email": "...", "role": "USER", "iat": ..., "exp": ... }
// signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
//
// The client sends: Authorization: Bearer <token>
// We verify the signature — if valid, we trust the claims without hitting the DB.
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs  // default: 24 hours
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UUID userId, String email, String role) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(signingKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String extractEmail(String token) {
        return parseToken(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
