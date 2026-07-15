package com.robinhood.marketdata.service;

import com.robinhood.marketdata.dto.*;
import com.robinhood.marketdata.exception.InvalidMarketDataRequestException;
import com.robinhood.marketdata.exception.StockNotFoundException;
import com.robinhood.marketdata.provider.MockMarketDataProvider;
import com.robinhood.marketdata.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataServiceTest {

    private MarketDataRepository marketDataRepository;
    private MarketDataService marketDataService;

    @BeforeEach
    void setUp() {
        marketDataRepository = new MarketDataRepository();
        MockMarketDataProvider provider = new MockMarketDataProvider(marketDataRepository);
        marketDataService = new MarketDataService(provider);
    }

    @Test
    void searchStocks_BlankQuery_ThrowsException() {
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.searchStocks("", 10));
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.searchStocks("   ", 10));
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.searchStocks(null, 10));
    }

    @Test
    void searchStocks_InvalidLimit_ThrowsException() {
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.searchStocks("apple", 0));
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.searchStocks("apple", 51));
    }

    @Test
    void searchStocks_ValidQuery_ReturnsResults() {
        List<StockSearchResponse> results = marketDataService.searchStocks("apple", 10);
        assertFalse(results.isEmpty());
        assertEquals("AAPL", results.get(0).symbol());
    }

    @Test
    void getCompanyDetails_UnknownSymbol_ThrowsException() {
        assertThrows(StockNotFoundException.class, () -> marketDataService.getCompanyDetails("xyz"));
    }

    @Test
    void getCompanyDetails_ValidSymbol_ReturnsDetails() {
        CompanyDetailsResponse details = marketDataService.getCompanyDetails("aapl");
        assertEquals("AAPL", details.symbol());
        assertEquals("Apple Inc.", details.companyName());
    }

    @Test
    void getCurrentPrice_UnknownSymbol_ThrowsException() {
        assertThrows(StockNotFoundException.class, () -> marketDataService.getCurrentPrice("xyz"));
    }

    @Test
    void getCurrentPrice_ValidSymbol_ReturnsPrice() {
        CurrentPriceResponse price = marketDataService.getCurrentPrice("aapl");
        assertEquals("AAPL", price.symbol());
        assertEquals(BigDecimal.valueOf(182.52), price.currentPrice());
    }

    @Test
    void getPriceHistory_UnknownSymbol_ThrowsException() {
        assertThrows(StockNotFoundException.class, () -> marketDataService.getPriceHistory("xyz", "ALL"));
    }

    @Test
    void getPriceHistory_InvalidRange_ThrowsException() {
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.getPriceHistory("aapl", "INVALID"));
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.getPriceHistory("aapl", ""));
        assertThrows(InvalidMarketDataRequestException.class, () -> marketDataService.getPriceHistory("aapl", null));
    }

    @Test
    void getPriceHistory_ValidSymbol_ReturnsHistory() {
        List<PriceHistoryResponse> history = marketDataService.getPriceHistory("aapl", "ALL");
        assertFalse(history.isEmpty());
        assertEquals("AAPL", history.get(0).symbol());
    }
}
