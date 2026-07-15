package com.robinhood.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceHistoryResponse(
        String symbol,
        LocalDate date,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        Long volume
) {
}