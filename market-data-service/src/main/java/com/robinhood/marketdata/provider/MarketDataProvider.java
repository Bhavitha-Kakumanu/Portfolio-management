package com.robinhood.marketdata.provider;

import com.robinhood.marketdata.dto.*;
import java.util.List;

public interface MarketDataProvider {
    List<StockSearchResponse> searchStocks(String query, int limit);
    CompanyDetailsResponse getCompanyDetails(String symbol);
    CurrentPriceResponse getCurrentPrice(String symbol);
    List<PriceHistoryResponse> getPriceHistory(String symbol, String range);
    List<TopMoversResponse> getTopGainers();
    List<TopMoversResponse> getTopLosers();
    boolean existsBySymbol(String symbol);
}
