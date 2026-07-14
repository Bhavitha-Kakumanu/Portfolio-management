package com.robinhood.entity;

import java.time.LocalDateTime;

import com.robinhood.enums.TradeStatus;
import com.robinhood.enums.TradeType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    private Integer quantity;
    private Double price;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    private LocalDateTime createdAt;
}