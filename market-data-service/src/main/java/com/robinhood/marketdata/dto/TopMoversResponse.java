package com.robinhood.marketdata.dto;

import java.math.BigDecimal;

public record TopMoversResponse(
        String symbol,
        String companyName,
        BigDecimal currentPrice,
        BigDecimal changePercent
) {
}