package com.robinhood.marketdata.service;

import com.robinhood.marketdata.dto.*;
import com.robinhood.marketdata.exception.InvalidMarketDataRequestException;
import com.robinhood.marketdata.exception.StockNotFoundException;
import com.robinhood.marketdata.provider.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);
    private final MarketDataProvider marketDataProvider;

    public MarketDataService(MarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    public List<StockSearchResponse> searchStocks(String query, int limit) {
        log.info("Searching stocks with query: {}, limit: {}", query, limit);
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidMarketDataRequestException("Search query must not be blank.");
        }
        if (limit < 1 || limit > 50) {
            throw new InvalidMarketDataRequestException("Search limit must be between 1 and 50.");
        }
        return marketDataProvider.searchStocks(query.trim(), limit);
    }

    public CompanyDetailsResponse getCompanyDetails(String symbol) {
        log.info("Fetching company details for symbol: {}", symbol);
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidMarketDataRequestException("Symbol must not be blank.");
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        CompanyDetailsResponse details = marketDataProvider.getCompanyDetails(normalizedSymbol);
        if (details == null) {
            log.warn("Stock not found: {}", normalizedSymbol);
            throw new StockNotFoundException(normalizedSymbol);
        }
        return details;
    }

    public CurrentPriceResponse getCurrentPrice(String symbol) {
        log.info("Fetching current price for symbol: {}", symbol);
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidMarketDataRequestException("Symbol must not be blank.");
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        CurrentPriceResponse price = marketDataProvider.getCurrentPrice(normalizedSymbol);
        if (price == null) {
            log.warn("Stock not found: {}", normalizedSymbol);
            throw new StockNotFoundException(normalizedSymbol);
        }
        return price;
    }

    public List<PriceHistoryResponse> getPriceHistory(String symbol, String range) {
        log.info("Fetching price history for symbol: {}, range: {}", symbol, range);
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidMarketDataRequestException("Symbol must not be blank.");
        }
        if (range == null || range.trim().isEmpty()) {
            throw new InvalidMarketDataRequestException("Range must not be blank.");
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        String normalizedRange = range.trim().toUpperCase();
        if (!normalizedRange.matches("^(1D|1W|1M|3M|1Y|ALL)$")) {
            throw new InvalidMarketDataRequestException("Invalid history range. Supported ranges: 1D, 1W, 1M, 3M, 1Y, ALL");
        }
        if (!marketDataProvider.existsBySymbol(normalizedSymbol)) {
            log.warn("Stock not found: {}", normalizedSymbol);
            throw new StockNotFoundException(normalizedSymbol);
        }
        return marketDataProvider.getPriceHistory(normalizedSymbol, normalizedRange);
    }

    public List<TopMoversResponse> getTopGainers() {
        log.info("Fetching top gainers");
        return marketDataProvider.getTopGainers();
    }

    public List<TopMoversResponse> getTopLosers() {
        log.info("Fetching top losers");
        return marketDataProvider.getTopLosers();
    }
}