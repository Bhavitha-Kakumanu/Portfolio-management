package com.robinhood.marketdata.dto;

public record StockSearchResponse(
        String symbol,
        String companyName,
        String exchange,
        String currency
) {
}