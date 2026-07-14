package com.robinhood.dto;

import com.robinhood.enums.TradeType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeRequest {

    private Long userId;
    private String stockSymbol;
    private TradeType tradeType;
    private Integer quantity;
    private Double price;
}