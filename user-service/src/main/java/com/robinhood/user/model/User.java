package com.robinhood.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// @Entity tells JPA this class maps to a database table
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    })
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    // UUID as primary key — better than auto-increment integers across microservices
    // because services can generate IDs independently without DB coordination
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    // Never store plaintext passwords. This field holds the BCrypt hash.
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @Column(nullable = false)
    private Double cashBalance = 0.0;

    @Column(nullable = false)
    private Double portfolioValue = 0.0;

    @Column(nullable = false)
    private Double totalGainLoss = 0.0;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum Role {
        USER, ADMIN
    }
}
