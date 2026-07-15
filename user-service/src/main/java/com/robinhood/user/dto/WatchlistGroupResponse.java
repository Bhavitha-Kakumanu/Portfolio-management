package com.robinhood.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class WatchlistGroupResponse {
    private UUID id;
    private String name;
    private Instant createdAt;
    private int itemCount;
}
