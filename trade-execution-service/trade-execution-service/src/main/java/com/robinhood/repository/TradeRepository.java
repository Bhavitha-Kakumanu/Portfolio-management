package com.robinhood.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robinhood.entity.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserId(Long userId);

    List<Trade> findByStockSymbolIgnoreCase(String stockSymbol);
}