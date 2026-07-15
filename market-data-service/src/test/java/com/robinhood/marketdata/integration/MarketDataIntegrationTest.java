package com.robinhood.marketdata.integration;

import com.robinhood.marketdata.dto.CompanyDetailsResponse;
import com.robinhood.marketdata.dto.CurrentPriceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MarketDataIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testEndToEndStockDetailsRetrieval() {
        String baseUrl = "http://localhost:" + port + "/api/v1/market";

        // 1. Get company details
        ResponseEntity<CompanyDetailsResponse> companyEntity = restTemplate.getForEntity(
                baseUrl + "/company/TSLA",
                CompanyDetailsResponse.class
        );
        assertEquals(HttpStatus.OK, companyEntity.getStatusCode());
        assertNotNull(companyEntity.getBody());
        assertEquals("TSLA", companyEntity.getBody().symbol());
        assertEquals("Tesla Inc.", companyEntity.getBody().companyName());

        // 2. Get current price
        ResponseEntity<CurrentPriceResponse> priceEntity = restTemplate.getForEntity(
                baseUrl + "/price/TSLA",
                CurrentPriceResponse.class
        );
        assertEquals(HttpStatus.OK, priceEntity.getStatusCode());
        assertNotNull(priceEntity.getBody());
        assertEquals("TSLA", priceEntity.getBody().symbol());
        assertEquals(BigDecimal.valueOf(248.87), priceEntity.getBody().currentPrice());
    }
}
