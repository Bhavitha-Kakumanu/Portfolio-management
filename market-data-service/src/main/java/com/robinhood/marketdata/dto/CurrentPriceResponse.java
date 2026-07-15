package com.robinhood.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CurrentPriceResponse(
        String symbol,
        BigDecimal currentPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        LocalDateTime lastUpdated
) {
}