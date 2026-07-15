package com.robinhood.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "symbol", "watchlist_group_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "watchlist_group_id")
    private WatchlistGroup watchlistGroup;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
