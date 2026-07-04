package com.robinhood.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String symbol;
    private int quantity;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TradeType type;

    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    public enum TradeType { BUY, SELL }
    public enum TradeStatus { PENDING, COMPLETED, FAILED }
}