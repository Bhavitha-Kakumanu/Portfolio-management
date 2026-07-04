package com.robinhood.trading.service;

import com.robinhood.trading.dto.TradeRequest;
import com.robinhood.trading.dto.TradeResponse;
import com.robinhood.trading.model.Trade;
import com.robinhood.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TradeResponse buy(UUID userId, TradeRequest request) {
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setSymbol(request.symbol().toUpperCase());
        trade.setQuantity(request.quantity());
        trade.setPrice(BigDecimal.valueOf(100.00)); // mock price for now
        trade.setType(Trade.TradeType.BUY);
        trade.setStatus(Trade.TradeStatus.COMPLETED);

        Trade saved = tradeRepository.save(trade);

        // notify other services via Kafka
        kafkaTemplate.send("order.placed",
            "BUY:" + request.symbol() + ":" + request.quantity() + ":user:" + userId);

        return TradeResponse.from(saved);
    }

    public TradeResponse sell(UUID userId, TradeRequest request) {
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setSymbol(request.symbol().toUpperCase());
        trade.setQuantity(request.quantity());
        trade.setPrice(BigDecimal.valueOf(100.00)); // mock price for now
        trade.setType(Trade.TradeType.SELL);
        trade.setStatus(Trade.TradeStatus.COMPLETED);

        Trade saved = tradeRepository.save(trade);

        kafkaTemplate.send("order.placed",
            "SELL:" + request.symbol() + ":" + request.quantity() + ":user:" + userId);

        return TradeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getHistory(UUID userId) {
        return tradeRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(TradeResponse::from)
            .toList();
    }
}