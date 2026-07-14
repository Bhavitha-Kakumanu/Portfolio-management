package com.robinhood.marketdata.provider;

import com.robinhood.marketdata.dto.*;
import com.robinhood.marketdata.repository.MarketDataRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockMarketDataProvider implements MarketDataProvider {

    private final MarketDataRepository repository;

    public MockMarketDataProvider(MarketDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StockSearchResponse> searchStocks(String query, int limit) {
        List<StockSearchResponse> all = repository.searchStocks(query);
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    @Override
    public CompanyDetailsResponse getCompanyDetails(String symbol) {
        return repository.findCompanyDetails(symbol).orElse(null);
    }

    @Override
    public CurrentPriceResponse getCurrentPrice(String symbol) {
        return repository.findCurrentPrice(symbol).orElse(null);
    }

    @Override
    public List<PriceHistoryResponse> getPriceHistory(String symbol, String range) {
        return repository.findPriceHistory(symbol, range);
    }

    @Override
    public List<TopMoversResponse> getTopGainers() {
        return repository.findTopGainers();
    }

    @Override
    public List<TopMoversResponse> getTopLosers() {
        return repository.findTopLosers();
    }

    @Override
    public boolean existsBySymbol(String symbol) {
        return repository.existsBySymbol(symbol);
    }
}
