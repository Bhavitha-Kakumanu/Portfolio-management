package com.robinhood.marketdata.repository;

import com.robinhood.marketdata.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataRepositoryTest {

    private MarketDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MarketDataRepository();
    }

    @Test
    void searchStocks_BySymbol() {
        List<StockSearchResponse> results = repository.searchStocks("AAPL");
        assertEquals(1, results.size());
        assertEquals("AAPL", results.get(0).symbol());
        assertEquals("Apple Inc.", results.get(0).companyName());
    }

    @Test
    void searchStocks_ByCompanyName() {
        List<StockSearchResponse> results = repository.searchStocks("Tesla");
        assertEquals(1, results.size());
        assertEquals("TSLA", results.get(0).symbol());
    }

    @Test
    void searchStocks_CaseInsensitiveAndSpaces() {
        List<StockSearchResponse> results = repository.searchStocks("  nvda  ");
        assertEquals(1, results.size());
        assertEquals("NVDA", results.get(0).symbol());
    }

    @Test
    void searchStocks_PartialMatch() {
        List<StockSearchResponse> results = repository.searchStocks("tsl");
        assertEquals(1, results.size());
        assertEquals("TSLA", results.get(0).symbol());
    }

    @Test
    void searchStocks_UnknownQuery() {
        List<StockSearchResponse> results = repository.searchStocks("XYZ");
        assertTrue(results.isEmpty());
    }

    @Test
    void findCompanyDetails_Success() {
        Optional<CompanyDetailsResponse> details = repository.findCompanyDetails("AAPL");
        assertTrue(details.isPresent());
        assertEquals("Apple Inc.", details.get().companyName());
        assertEquals("NASDAQ", details.get().exchange());
    }

    @Test
    void findCompanyDetails_NotFound() {
        Optional<CompanyDetailsResponse> details = repository.findCompanyDetails("XYZ");
        assertTrue(details.isEmpty());
    }

    @Test
    void findCurrentPrice_Success() {
        Optional<CurrentPriceResponse> price = repository.findCurrentPrice("TSLA");
        assertTrue(price.isPresent());
        assertEquals(BigDecimal.valueOf(248.87), price.get().currentPrice());
    }

    @Test
    void findCurrentPrice_NotFound() {
        Optional<CurrentPriceResponse> price = repository.findCurrentPrice("XYZ");
        assertTrue(price.isEmpty());
    }

    @Test
    void findPriceHistory_Success() {
        List<PriceHistoryResponse> history = repository.findPriceHistory("NVDA", "ALL");
        assertTrue(history.size() >= 200);
        assertEquals("NVDA", history.get(0).symbol());

        List<PriceHistoryResponse> history1D = repository.findPriceHistory("NVDA", "1D");
        assertEquals(1, history1D.size());

        List<PriceHistoryResponse> history1W = repository.findPriceHistory("NVDA", "1W");
        assertEquals(5, history1W.size());
    }

    @Test
    void findPriceHistory_NotFound() {
        List<PriceHistoryResponse> history = repository.findPriceHistory("XYZ", "ALL");
        assertTrue(history.isEmpty());
    }

    @Test
    void findTopGainers() {
        List<TopMoversResponse> gainers = repository.findTopGainers();
        assertFalse(gainers.isEmpty());
        for (int i = 0; i < gainers.size() - 1; i++) {
            assertTrue(gainers.get(i).changePercent().compareTo(gainers.get(i + 1).changePercent()) >= 0);
        }
    }

    @Test
    void findTopLosers() {
        List<TopMoversResponse> losers = repository.findTopLosers();
        assertFalse(losers.isEmpty());
        for (int i = 0; i < losers.size() - 1; i++) {
            assertTrue(losers.get(i).changePercent().compareTo(losers.get(i + 1).changePercent()) <= 0);
        }
    }

    @Test
    void existsBySymbol() {
        assertTrue(repository.existsBySymbol("AAPL"));
        assertFalse(repository.existsBySymbol("XYZ"));
    }

    @Test
    void testHistoricalDataInvariants() {
        for (String symbol : List.of("AAPL", "TSLA", "NVDA", "GOOGL", "AMZN", "META", "MSFT")) {
            List<PriceHistoryResponse> history = repository.findPriceHistory(symbol, "ALL");
            assertFalse(history.isEmpty(), "History should not be empty for " + symbol);

            java.util.Set<java.time.LocalDate> dates = new java.util.HashSet<>();
            for (PriceHistoryResponse candle : history) {
                assertEquals(symbol, candle.symbol());

                // Prices never become negative
                assertTrue(candle.openPrice().compareTo(BigDecimal.ZERO) > 0, "Open price should be positive");
                assertTrue(candle.highPrice().compareTo(BigDecimal.ZERO) > 0, "High price should be positive");
                assertTrue(candle.lowPrice().compareTo(BigDecimal.ZERO) > 0, "Low price should be positive");
                assertTrue(candle.closePrice().compareTo(BigDecimal.ZERO) > 0, "Close price should be positive");

                // High >= open & close
                assertTrue(candle.highPrice().compareTo(candle.openPrice()) >= 0, "High must be >= open");
                assertTrue(candle.highPrice().compareTo(candle.closePrice()) >= 0, "High must be >= close");

                // Low <= open & close
                assertTrue(candle.lowPrice().compareTo(candle.openPrice()) <= 0, "Low must be <= open");
                assertTrue(candle.lowPrice().compareTo(candle.closePrice()) <= 0, "Low must be <= close");

                // Volume never negative
                assertTrue(candle.volume() >= 0, "Volume must not be negative");

                // Dates contain no duplicates
                assertTrue(dates.add(candle.date()), "Found duplicate date: " + candle.date());

                // Weekends are handled consistently (no weekends)
                assertTrue(candle.date().getDayOfWeek().getValue() < 6, "History must not contain weekends: " + candle.date());
            }
        }
    }
}
