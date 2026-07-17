package com.robinhood.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robinhood.dto.TradeRequest;
import com.robinhood.dto.TradeResponse;
import com.robinhood.service.TradeService;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeResponse> executeTrade(
            @RequestBody TradeRequest request) {

        TradeResponse response = tradeService.executeTrade(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeResponse> getTradeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(tradeService.getTradeById(id));
    }
    @GetMapping("/stock/{stockSymbol}")
    public List<TradeResponse> getTradesByStockSymbol(
            @PathVariable String stockSymbol) {
    
        return tradeService.getTradesByStockSymbol(stockSymbol);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TradeResponse>> getTradesByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                tradeService.getTradesByUserId(userId)
        );
    }
}