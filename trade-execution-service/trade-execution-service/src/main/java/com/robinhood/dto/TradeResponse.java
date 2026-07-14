package com.robinhood.dto;

import java.time.LocalDateTime;

import com.robinhood.enums.TradeStatus;
import com.robinhood.enums.TradeType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeResponse {

    private Long id;
    private Long userId;
    private String stockSymbol;
    private TradeType tradeType;
    private Integer quantity;
    private Double price;
    private Double totalAmount;
    private TradeStatus status;
    private LocalDateTime createdAt;
}