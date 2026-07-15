package com.robinhood.marketdata.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String requestId,
        LocalDateTime timestamp
) {}
