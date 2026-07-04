package com.robinhood.trading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TradeRequest(
    @NotBlank String symbol,
    @NotNull @Min(1) int quantity
) {}