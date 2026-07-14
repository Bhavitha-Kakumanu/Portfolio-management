package com.robinhood.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.robinhood.exception.TradeNotFoundException;
import org.springframework.stereotype.Service;
import com.robinhood.dto.TradeRequest;
import com.robinhood.dto.TradeResponse;
import com.robinhood.entity.Trade;
import com.robinhood.enums.TradeStatus;
import com.robinhood.repository.TradeRepository;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;

    public TradeServiceImpl(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public TradeResponse executeTrade(TradeRequest request) {

        Trade trade = new Trade();

        trade.setUserId(request.getUserId());
        trade.setStockSymbol(request.getStockSymbol());
        trade.setTradeType(request.getTradeType());
        trade.setQuantity(request.getQuantity());
        trade.setPrice(request.getPrice());

        double totalAmount =
                request.getQuantity() * request.getPrice();

        trade.setTotalAmount(totalAmount);
        trade.setStatus(TradeStatus.COMPLETED);
        trade.setCreatedAt(LocalDateTime.now());

        Trade savedTrade = tradeRepository.save(trade);

        return convertToResponse(savedTrade);
    }

    @Override
    public TradeResponse getTradeById(Long id) {

        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() ->
                        new TradeNotFoundException("Trade not found with id: " + id));

        return convertToResponse(trade);
    }

    @Override
    public List<TradeResponse> getTradesByUserId(Long userId) {

        return tradeRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TradeResponse convertToResponse(Trade trade) {

        TradeResponse response = new TradeResponse();

        response.setId(trade.getId());
        response.setUserId(trade.getUserId());
        response.setStockSymbol(trade.getStockSymbol());
        response.setTradeType(trade.getTradeType());
        response.setQuantity(trade.getQuantity());
        response.setPrice(trade.getPrice());
        response.setTotalAmount(trade.getTotalAmount());
        response.setStatus(trade.getStatus());
        response.setCreatedAt(trade.getCreatedAt());

        return response;
    }
}