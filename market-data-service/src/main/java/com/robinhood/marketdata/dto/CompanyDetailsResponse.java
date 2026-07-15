package com.robinhood.marketdata.dto;

public record CompanyDetailsResponse(
        String symbol,
        String companyName,
        String sector,
        String industry,
        String description,
        String exchange,
        String currency
) {
}