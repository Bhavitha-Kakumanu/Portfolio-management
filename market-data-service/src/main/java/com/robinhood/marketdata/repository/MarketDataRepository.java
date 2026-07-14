package com.robinhood.marketdata.repository;

import com.robinhood.marketdata.dto.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class MarketDataRepository {

    private final Map<String, CompanyDetailsResponse> companyDetailsMap = new HashMap<>();
    private final Map<String, CurrentPriceResponse> currentPriceMap = new HashMap<>();
    private final Map<String, List<PriceHistoryResponse>> priceHistoryMap = new HashMap<>();
    private final List<StockSearchResponse> searchStocksList = new ArrayList<>();

    public MarketDataRepository() {
        initMockData();
    }

    private void initMockData() {
        // AAPL
        addStock(
                "AAPL", "Apple Inc.", "NASDAQ", "USD", "Technology", "Consumer Electronics",
                "Apple Inc. designs, manufactures, and sells smartphones, personal computers, tablets, wearables, and accessories, and sells a variety of related services.",
                182.52, 2.24, 1.24, 1700000L
        );

        // TSLA
        addStock(
                "TSLA", "Tesla Inc.", "NASDAQ", "USD", "Consumer Cyclical", "Auto Manufacturers",
                "Tesla, Inc. designs, develops, manufactures, sells, and leases fully electric vehicles, energy generation and storage systems, and offers services related to its products.",
                248.87, -5.37, -2.11, 2200000L
        );

        // NVDA
        addStock(
                "NVDA", "NVIDIA Corporation", "NASDAQ", "USD", "Technology", "Semiconductors",
                "NVIDIA Corporation designs graphics processing units for the gaming and professional markets, as well as system on a chip units for the mobile computing and automotive market.",
                891.32, 32.48, 3.78, 3100000L
        );

        // GOOGL
        addStock(
                "GOOGL", "Alphabet Inc.", "NASDAQ", "USD", "Technology", "Internet Content & Information",
                "Alphabet Inc. provides online advertising services, search engine technology, cloud computing, computer software, hardware, and e-commerce services.",
                143.66, 1.20, 0.84, 1400000L
        );

        // AMZN
        addStock(
                "AMZN", "Amazon.com Inc.", "NASDAQ", "USD", "Consumer Cyclical", "Internet Retail",
                "Amazon.com, Inc. focuses on e-commerce, cloud computing, online advertising, digital streaming, and artificial intelligence.",
                186.40, -1.73, -0.92, 1900000L
        );

        // META
        addStock(
                "META", "Meta Platforms Inc.", "NASDAQ", "USD", "Technology", "Internet Content & Information",
                "Meta Platforms, Inc. focuses on building products that enable people to connect and share through mobile devices, personal computers, virtual reality headsets, and wearables.",
                476.20, -6.52, -1.35, 1500000L
        );

        // MSFT
        addStock(
                "MSFT", "Microsoft Corporation", "NASDAQ", "USD", "Technology", "Software - Infrastructure",
                "Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide, including Windows, Office, Azure, and Xbox.",
                415.50, 4.10, 1.00, 2000000L
        );
    }

    private void addStock(
            String symbol, String companyName, String exchange, String currency,
            String sector, String industry, String description,
            double currentPrice, double changeAmount, double changePercent, long volume
    ) {
        String upperSymbol = symbol.toUpperCase();

        searchStocksList.add(new StockSearchResponse(upperSymbol, companyName, exchange, currency));
        companyDetailsMap.put(upperSymbol, new CompanyDetailsResponse(
                upperSymbol, companyName, sector, industry, description, exchange, currency
        ));
        currentPriceMap.put(upperSymbol, new CurrentPriceResponse(
                upperSymbol, BigDecimal.valueOf(currentPrice), BigDecimal.valueOf(changeAmount), BigDecimal.valueOf(changePercent), LocalDateTime.of(2026, 7, 14, 15, 30, 0)
        ));

        // Generate 300 trading days of history
        List<PriceHistoryResponse> history = generateHistory(upperSymbol, currentPrice, volume);
        priceHistoryMap.put(upperSymbol, history);
    }

    private List<PriceHistoryResponse> generateHistory(String symbol, double currentPrice, long baseVolume) {
        List<PriceHistoryResponse> history = new ArrayList<>();
        LocalDate today = LocalDate.of(2026, 7, 14);
        double closePrice = currentPrice;
        Random rand = new Random(symbol.hashCode()); // deterministic seed per symbol

        for (int i = 0; i < 300; i++) {
            LocalDate date = today.minusDays(i);
            // Skip weekends
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }

            double changePercent = (rand.nextDouble() - 0.485) * 0.035; // slight upward drift going forward
            double openPrice = closePrice / (1 + changePercent);
            double highPrice = Math.max(openPrice, closePrice) * (1 + rand.nextDouble() * 0.01);
            double lowPrice = Math.min(openPrice, closePrice) * (1 - rand.nextDouble() * 0.01);
            long vol = (long) (baseVolume * (0.6 + rand.nextDouble() * 0.8));

            history.add(0, new PriceHistoryResponse(
                    symbol,
                    date,
                    BigDecimal.valueOf(openPrice),
                    BigDecimal.valueOf(highPrice),
                    BigDecimal.valueOf(lowPrice),
                    BigDecimal.valueOf(closePrice),
                    vol
            ));

            closePrice = openPrice;
        }
        return history;
    }

    public List<StockSearchResponse> searchStocks(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String normalizedQuery = query.trim().toLowerCase();
        return searchStocksList.stream()
                .filter(stock -> stock.symbol().toLowerCase().contains(normalizedQuery) ||
                        stock.companyName().toLowerCase().contains(normalizedQuery))
                .toList();
    }

    public Optional<CompanyDetailsResponse> findCompanyDetails(String symbol) {
        if (symbol == null) return Optional.empty();
        return Optional.ofNullable(companyDetailsMap.get(symbol.toUpperCase()));
    }

    public Optional<CurrentPriceResponse> findCurrentPrice(String symbol) {
        if (symbol == null) return Optional.empty();
        return Optional.ofNullable(currentPriceMap.get(symbol.toUpperCase()));
    }

    public List<PriceHistoryResponse> findPriceHistory(String symbol, String range) {
        if (symbol == null) return Collections.emptyList();
        List<PriceHistoryResponse> history = priceHistoryMap.get(symbol.toUpperCase());
        if (history == null) return Collections.emptyList();

        int limit;
        switch (range.toUpperCase()) {
            case "1D":
                limit = 1;
                break;
            case "1W":
                limit = 5;
                break;
            case "1M":
                limit = 21;
                break;
            case "3M":
                limit = 63;
                break;
            case "1Y":
                limit = 252;
                break;
            case "ALL":
            default:
                limit = history.size();
                break;
        }

        int size = history.size();
        if (limit >= size) {
            return history;
        }
        return history.subList(size - limit, size);
    }

    public List<TopMoversResponse> findTopGainers() {
        return currentPriceMap.values().stream()
                .filter(p -> p.changePercent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((p1, p2) -> p2.changePercent().compareTo(p1.changePercent()))
                .map(p -> new TopMoversResponse(
                        p.symbol(),
                        companyDetailsMap.get(p.symbol()).companyName(),
                        p.currentPrice(),
                        p.changePercent()
                ))
                .limit(3)
                .toList();
    }

    public List<TopMoversResponse> findTopLosers() {
        return currentPriceMap.values().stream()
                .filter(p -> p.changePercent().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(CurrentPriceResponse::changePercent))
                .map(p -> new TopMoversResponse(
                        p.symbol(),
                        companyDetailsMap.get(p.symbol()).companyName(),
                        p.currentPrice(),
                        p.changePercent()
                ))
                .limit(3)
                .toList();
    }

    public boolean existsBySymbol(String symbol) {
        if (symbol == null) return false;
        return companyDetailsMap.containsKey(symbol.toUpperCase());
    }
}
