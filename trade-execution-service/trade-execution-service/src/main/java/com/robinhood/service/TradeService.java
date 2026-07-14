package com.robinhood.service;

import java.util.List;

import com.robinhood.dto.TradeRequest;
import com.robinhood.dto.TradeResponse;

public interface TradeService {

    TradeResponse executeTrade(TradeRequest request);

    TradeResponse getTradeById(Long id);

    List<TradeResponse> getTradesByUserId(Long userId);
}