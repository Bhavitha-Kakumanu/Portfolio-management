# Market Data Service

Market Data Service provides stock search, company details, current prices, historical prices, top gainers, and top losers.

## Configuration

* **Port**: `8083`
* **Current Limitation**: The service currently returns in-memory mock market data.

## Run Command

To run the service locally:

```bash
mvn spring-boot:run
```

## API Endpoint Documentation

All endpoints are versioned under `/api/v1/market`.

### 1. Stock Search
* **Endpoint**: `GET /api/v1/market/search`
* **Query Parameters**: 
  * `query` (String, required, cannot be blank)
  * `limit` (Integer, optional, default: `10`, range: `1` to `50`)
* **Response**: List of stocks matching symbol or company name up to the limit.

### 2. Company Details
* **Endpoint**: `GET /api/v1/market/company/{symbol}`
* **Path Parameters**: `symbol` (String, required, e.g. `AAPL`)
* **Response**: Detailed company information including sector, industry, description.

### 3. Current Price
* **Endpoint**: `GET /api/v1/market/price/{symbol}`
* **Path Parameters**: `symbol` (String, required, e.g. `TSLA`)
* **Response**: Current quote details, including price change amount and percentage.

### 4. Price History
* **Endpoint**: `GET /api/v1/market/history/{symbol}`
* **Path Parameters**: `symbol` (String, required, e.g. `NVDA`)
* **Query Parameters**:
  * `range` (String, optional, default: `ALL`). Supported values: `1D`, `1W`, `1M`, `3M`, `1Y`, `ALL`.
* **Example**: `/api/v1/market/history/TSLA?range=1M`
* **Response**: Historical list of daily OHLC (open, high, low, close) and volume data sliced to the selected range.

### 5. Top Gainers
* **Endpoint**: `GET /api/v1/market/top-gainers`
* **Response**: Top 3 mover stocks with positive day change percentages.

### 6. Top Losers
* **Endpoint**: `GET /api/v1/market/top-losers`
* **Response**: Top 3 mover stocks with negative day change percentages.

## Swagger / OpenAPI Docs

When running, Swagger UI is available at:
`http://localhost:8083/swagger-ui/index.html`
