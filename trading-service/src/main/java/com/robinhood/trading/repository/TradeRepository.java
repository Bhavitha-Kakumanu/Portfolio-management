package com.robinhood.trading.repository;

import com.robinhood.trading.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByUserIdOrderByCreatedAtDesc(UUID userId);
}