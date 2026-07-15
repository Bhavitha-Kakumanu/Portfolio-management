package com.robinhood.marketdata.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MarketDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchStocks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/search")
                        .param("query", "apple")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."));
    }

    @Test
    void searchStocks_InvalidLimit_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/market/search")
                        .param("query", "apple")
                        .param("limit", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/market/search")
                        .param("query", "apple")
                        .param("limit", "51")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStocks_BlankQuery_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/market/search")
                        .param("query", "   ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompanyDetails_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/company/AAPL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.companyName").value("Apple Inc."));
    }

    @Test
    void getCompanyDetails_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/market/company/XYZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Stock not found: XYZ"));
    }

    @Test
    void getCurrentPrice_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/price/AAPL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.currentPrice").value(182.52));
    }

    @Test
    void getPriceHistory_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/history/AAPL")
                        .param("range", "1W")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].volume").exists());
    }

    @Test
    void getPriceHistory_InvalidRange_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/market/history/AAPL")
                        .param("range", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopGainers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/top-gainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("NVDA"));
    }

    @Test
    void getTopLosers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/market/top-losers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("TSLA"));
    }
}
