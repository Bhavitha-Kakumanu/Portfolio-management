package com.robinhood.trading.dto;

import com.robinhood.trading.model.Trade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeResponse(
    UUID id,
    UUID userId,
    String symbol,
    int quantity,
    BigDecimal price,
    String type,
    String status,
    Instant createdAt
) {
    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
            trade.getId(),
            trade.getUserId(),
            trade.getSymbol(),
            trade.getQuantity(),
            trade.getPrice(),
            trade.getType().name(),
            trade.getStatus().name(),
            trade.getCreatedAt()
        );
    }
}