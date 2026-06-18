package com.robinhood.trading.controller;

import com.robinhood.trading.dto.TradeRequest;
import com.robinhood.trading.dto.TradeResponse;
import com.robinhood.trading.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.buy(UUID.fromString(userId), request));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.sell(UUID.fromString(userId), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TradeResponse>> history(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(tradeService.getHistory(UUID.fromString(userId)));
    }
}