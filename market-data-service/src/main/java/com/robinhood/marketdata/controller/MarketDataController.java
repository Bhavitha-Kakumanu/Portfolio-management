package com.robinhood.marketdata.controller;

import com.robinhood.marketdata.dto.*;
import com.robinhood.marketdata.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@Validated
@Tag(name = "Market Data API", description = "Endpoints for searching stocks and retrieving price details, histories, and top movers")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search stocks", description = "Search stocks by ticker symbol or company name (case-insensitive, partial matching supported).")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<StockSearchResponse> searchStocks(
            @RequestParam
            @NotBlank(message = "Search query must not be blank")
            @Parameter(description = "Query string matching ticker symbol or company name", required = true)
            String query,
            @RequestParam(required = false, defaultValue = "10")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            @Parameter(description = "Maximum number of search results to return (1-50)", required = false)
            int limit) {
        return marketDataService.searchStocks(query, limit);
    }

    @GetMapping("/company/{symbol}")
    @Operation(summary = "Get company details", description = "Fetch detailed company profile information for the specified stock symbol.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "400", description = "Invalid symbol parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Stock not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CompanyDetailsResponse getCompanyDetails(
            @PathVariable
            @NotBlank(message = "Symbol must not be blank")
            @Parameter(description = "The ticker symbol of the stock (e.g. AAPL)", required = true)
            String symbol) {
        return marketDataService.getCompanyDetails(symbol);
    }

    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get current price", description = "Fetch the current real-time/latest price details including day change metrics for the specified stock symbol.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "400", description = "Invalid symbol parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Stock not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CurrentPriceResponse getCurrentPrice(
            @PathVariable
            @NotBlank(message = "Symbol must not be blank")
            @Parameter(description = "The ticker symbol of the stock (e.g. TSLA)", required = true)
            String symbol) {
        return marketDataService.getCurrentPrice(symbol);
    }

    @GetMapping("/history/{symbol}")
    @Operation(summary = "Get price history", description = "Fetch daily price history candles (open, high, low, close, volume) for the specified stock symbol.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "400", description = "Invalid symbol parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Stock not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<PriceHistoryResponse> getPriceHistory(
            @PathVariable
            @NotBlank(message = "Symbol must not be blank")
            @Parameter(description = "The ticker symbol of the stock (e.g. NVDA)", required = true)
            String symbol,
            @RequestParam(required = false, defaultValue = "ALL")
            @Pattern(regexp = "^(?i)(1D|1W|1M|3M|1Y|ALL)$", message = "Invalid history range. Supported ranges: 1D, 1W, 1M, 3M, 1Y, ALL")
            @Parameter(description = "The history range to filter (1D, 1W, 1M, 3M, 1Y, ALL)", required = false)
            String range) {
        return marketDataService.getPriceHistory(symbol, range);
    }

    @GetMapping("/top-gainers")
    @Operation(summary = "Get top gainers", description = "Fetch list of top-performing stocks sorted by largest price increase percent.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<TopMoversResponse> getTopGainers() {
        return marketDataService.getTopGainers();
    }

    @GetMapping("/top-losers")
    @Operation(summary = "Get top losers", description = "Fetch list of worst-performing stocks sorted by largest price decrease percent.")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<TopMoversResponse> getTopLosers() {
        return marketDataService.getTopLosers();
    }
}