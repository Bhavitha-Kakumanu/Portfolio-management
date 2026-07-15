package com.robinhood.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class WatchlistResponse {
    private UUID id;
    private String symbol;
    private Instant createdAt;
    private Boolean favorite;
    private UUID watchlistGroupId;
}
