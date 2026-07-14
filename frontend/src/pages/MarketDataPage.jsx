import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
    searchStocks,
    getCompanyDetails,
    getCurrentPrice,
    getPriceHistory,
    getTopGainers,
    getTopLosers,
} from '../api/market';
import PriceHistoryChart from '../components/PriceHistoryChart';

export default function MarketDataPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const urlSymbol = searchParams.get('symbol');

    const [stocks, setStocks] = useState([]);
    const [company, setCompany] = useState(null);
    const [price, setPrice] = useState(null);
    const [history, setHistory] = useState([]);
    const [gainers, setGainers] = useState([]);
    const [losers, setLosers] = useState([]);

    const [query, setQuery] = useState('');
    const [selectedSymbol, setSelectedSymbol] = useState(urlSymbol ? urlSymbol.toUpperCase() : 'AAPL');
    const [selectedRange, setSelectedRange] = useState('ALL');
    const [sortOrder, setSortOrder] = useState('desc'); // 'desc' (Newest -> Oldest) or 'asc' (Oldest -> Newest)

    // Loading states
    const [searchLoading, setSearchLoading] = useState(false);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [pageLoading, setPageLoading] = useState(true);

    // Error states
    const [error, setError] = useState('');
    const [searchError, setSearchError] = useState('');
    const [detailsError, setDetailsError] = useState('');

    // Refs
    const activeSymbolRef = useRef(selectedSymbol);
    const detailsRef = useRef(null);

    // Dynamic page title metadata with cleanup
    useEffect(() => {
        document.title = `${selectedSymbol} Market Data | Robinhood`;
        return () => {
            document.title = 'Robinhood';
        };
    }, [selectedSymbol]);

    const formatChangeInfo = (amount, percent) => {
        if (amount === null || percent === null || amount === undefined || percent === undefined) return '';
        const numAmt = Number(amount);
        const numPct = Number(percent);
        const amtSign = numAmt > 0 ? '+' : '';
        const pctSign = numPct > 0 ? '+' : '';
        const arrow = numPct >= 0 ? '▲' : '▼';
        return `${amtSign}${numAmt.toFixed(2)} ${arrow} ${pctSign}${numPct.toFixed(2)}%`;
    };

    const formatChangePercent = (percent) => {
        if (percent === null || percent === undefined) return '';
        const num = Number(percent);
        if (isNaN(num)) return String(percent);
        const arrow = num >= 0 ? '▲' : '▼';
        const sign = num > 0 ? '+' : '';
        return `${arrow} ${sign}${num.toFixed(2)}%`;
    };

    const loadStockDetails = useCallback(async (symbol, range = 'ALL') => {
        if (!symbol) return;
        const upperSymbol = symbol.trim().toUpperCase();
        setSelectedSymbol(upperSymbol);

        // Only update URL if different
        if (searchParams.get('symbol')?.toUpperCase() !== upperSymbol) {
            setSearchParams({ symbol: upperSymbol });
        }

        // Clear stale details immediately to prevent flashing old data
        setCompany(null);
        setPrice(null);
        setHistory([]);
        setDetailsError('');
        setDetailsLoading(true);

        activeSymbolRef.current = upperSymbol;

        try {
            const [companyRes, priceRes, historyRes] = await Promise.all([
                getCompanyDetails(upperSymbol),
                getCurrentPrice(upperSymbol),
                getPriceHistory(upperSymbol, range),
            ]);

            if (activeSymbolRef.current === upperSymbol) {
                setCompany(companyRes.data);
                setPrice(priceRes.data);
                setHistory(historyRes.data);

                // Scroll to details section smoothly
                setTimeout(() => {
                    detailsRef.current?.scrollIntoView({ behavior: 'smooth' });
                }, 50);
            }
        } catch (err) {
            if (activeSymbolRef.current === upperSymbol) {
                if (!err.response) {
                    setDetailsError('Market Data Service is unavailable.');
                } else {
                    setDetailsError(err.response.data?.message || 'Unable to load stock details.');
                }
            }
        } finally {
            if (activeSymbolRef.current === upperSymbol) {
                setDetailsLoading(false);
            }
        }
    }, [searchParams, setSearchParams]);

    async function handleSearch(e) {
        if (e) e.preventDefault();
        const trimmedQuery = query.trim();
        if (!trimmedQuery) {
            setSearchError('Enter a stock symbol or company name.');
            setStocks([]);
            return;
        }
        setSearchLoading(true);
        setSearchError('');
        try {
            const res = await searchStocks(trimmedQuery, 10);
            setStocks(res.data);
            if (res.data.length === 0) {
                setSearchError('No stocks found.');
            } else if (res.data.length === 1) {
                await loadStockDetails(res.data[0].symbol, selectedRange);
            }
        } catch (err) {
            if (!err.response) {
                setSearchError('Market Data Service is unavailable.');
            } else {
                setSearchError(err.response.data?.message || 'Search failed.');
            }
            setStocks([]);
        } finally {
            setSearchLoading(false);
        }
    }

    async function handleRangeSelect(range) {
        setSelectedRange(range);
        if (!selectedSymbol) return;
        await loadStockDetails(selectedSymbol, range);
    }

    const loadMoversData = useCallback(async () => {
        setPageLoading(true);
        setError('');
        try {
            const [gainersRes, losersRes] = await Promise.all([
                getTopGainers(),
                getTopLosers(),
            ]);
            setGainers(gainersRes.data);
            setLosers(losersRes.data);
        } catch {
            setError('No market-mover data is currently available.');
        } finally {
            setPageLoading(false);
        }
    }, []);

    useEffect(() => {
        loadMoversData();
        const initialSymbol = urlSymbol ? urlSymbol.toUpperCase() : 'AAPL';
        loadStockDetails(initialSymbol, 'ALL');
    }, [urlSymbol, loadMoversData, loadStockDetails]);

    // Sorted history based on date and user sort preference
    const sortedHistory = [...history].sort((a, b) => {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        return sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
    });

    const handleRowKeyDown = (event, symbol) => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            loadStockDetails(symbol, selectedRange);
        }
    };

    return (
        <main className="market-page">
            <section className="market-header">
                <p className="eyebrow">Market Overview</p>
                <h1 className="market-title">Market Data</h1>
                <p className="market-subtitle">
                    Search stocks, view company details, current price, price history, top gainers, and top losers.
                </p>
            </section>

            {error && (
                <div className="market-error-banner market-error-content">
                    <span>{error}</span>
                    <button className="btn market-retry-button" onClick={loadMoversData}>
                        Retry
                    </button>
                </div>
            )}

            <section className="market-card">
                <h2>Stock Search</h2>

                <form onSubmit={handleSearch} className="market-search-row">
                    <input
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        placeholder="Search stock, e.g. apple"
                    />
                    <button className="btn btn-green" type="submit" disabled={searchLoading}>
                        {searchLoading ? 'Searching...' : 'Search'}
                    </button>
                </form>

                {searchLoading ? (
                    <div className="market-loading">Searching...</div>
                ) : stocks.length > 0 ? (
                    <div>
                        <div className="market-result-count">
                            {stocks.length} {stocks.length === 1 ? 'result' : 'results'} found
                        </div>
                        <div className="market-table-wrapper">
                            <table className="market-table">
                                <thead>
                                <tr>
                                    <th>Symbol</th>
                                    <th>Company</th>
                                    <th>Exchange</th>
                                    <th>Currency</th>
                                </tr>
                                </thead>
                                <tbody>
                                {stocks.map((stock) => (
                                    <tr
                                        key={stock.symbol}
                                        tabIndex={0}
                                        role="button"
                                        aria-selected={selectedSymbol === stock.symbol}
                                        className={selectedSymbol === stock.symbol ? 'market-row selected' : 'market-row'}
                                        onClick={() => loadStockDetails(stock.symbol, selectedRange)}
                                        onKeyDown={(e) => handleRowKeyDown(e, stock.symbol)}
                                    >
                                        <td><strong>{stock.symbol}</strong></td>
                                        <td>{stock.companyName}</td>
                                        <td>{stock.exchange}</td>
                                        <td>{stock.currency}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                ) : (
                    searchError && <p className="empty-state">{searchError}</p>
                )}
            </section>

            <div ref={detailsRef}>
                {detailsLoading && !company && !price ? (
                    <div className="market-loading-container market-center-container">
                        <p className="market-loading">Loading stock details...</p>
                    </div>
                ) : detailsError ? (
                    <div className="market-error-container market-center-container">
                        <p className="market-error">{detailsError}</p>
                        <button className="btn btn-secondary market-margin-top" onClick={() => loadStockDetails(selectedSymbol, selectedRange)}>
                            Retry
                        </button>
                    </div>
                ) : (
                    <>
                        <section className="market-grid">
                            {price && (
                                <div className="market-card">
                                    <p className="eyebrow">Current Price</p>
                                    <div className="price-symbol">{price.symbol}</div>
                                    <div className="price-value">${Number(price.currentPrice).toFixed(2)}</div>
                                    <p className={Number(price.changePercent) > 0 ? 'positive' : Number(price.changePercent) < 0 ? 'negative' : 'neutral'}>
                                        {formatChangeInfo(price.changeAmount, price.changePercent)}
                                    </p>
                                    <p className="last-updated market-timestamp">
                                        Last Updated: {new Date(price.lastUpdated).toLocaleString()}
                                    </p>
                                </div>
                            )}

                            {company && (
                                <div className="market-card">
                                    <p className="eyebrow">Company Details</p>
                                    <h2>{company.companyName}</h2>
                                    <p><strong>Sector:</strong> {company.sector}</p>
                                    <p><strong>Industry:</strong> {company.industry}</p>
                                    <p><strong>Exchange:</strong> {company.exchange}</p>
                                    <p className="market-description">{company.description}</p>
                                </div>
                            )}
                        </section>

                        {company && (
                            <section className="market-card">
                                <div className="market-history-header">
                                    <h2>Price History</h2>

                                    {/* Date range filters */}
                                    <div className="range-filters market-filters-row">
                                        {['1D', '1W', '1M', '3M', '1Y', 'ALL'].map((r) => (
                                            <button
                                                key={r}
                                                className={selectedRange === r ? 'btn btn-green market-filter-btn' : 'btn market-filter-btn'}
                                                onClick={() => handleRangeSelect(r)}
                                            >
                                                {r}
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                {history && history.length > 0 ? (
                                    <>
                                        <PriceHistoryChart history={history} selectedSymbol={selectedSymbol} />

                                        <div className="market-table-header">
                                            <span className="market-table-title">Historical Data Table</span>
                                            <button
                                                className="btn market-retry-button"
                                                onClick={() => setSortOrder(prev => prev === 'desc' ? 'asc' : 'desc')}
                                            >
                                                Order: {sortOrder === 'desc' ? 'Newest → Oldest' : 'Oldest → Newest'}
                                            </button>
                                        </div>

                                        <div className="market-table-wrapper">
                                            <table className="market-table">
                                                <thead>
                                                <tr>
                                                    <th>Date</th>
                                                    <th>Open</th>
                                                    <th>High</th>
                                                    <th>Low</th>
                                                    <th>Close</th>
                                                    <th>Volume</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {sortedHistory.map((item) => (
                                                    <tr key={item.date}>
                                                        <td>{item.date}</td>
                                                        <td>${Number(item.openPrice).toFixed(2)}</td>
                                                        <td>${Number(item.highPrice).toFixed(2)}</td>
                                                        <td>${Number(item.lowPrice).toFixed(2)}</td>
                                                        <td>${Number(item.closePrice).toFixed(2)}</td>
                                                        <td>{Number(item.volume).toLocaleString()}</td>
                                                    </tr>
                                                ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </>
                                ) : (
                                    !detailsLoading && <p className="empty-state">No price history is available for this stock.</p>
                                )}
                            </section>
                        )}
                    </>
                )}
            </div>

            <section className="market-grid">
                <div className="market-card">
                    <h2>Top Profited Companies</h2>
                    {error ? (
                        <p className="empty-state">{error}</p>
                    ) : pageLoading ? (
                        <p className="market-loading">Loading movers...</p>
                    ) : gainers && gainers.length > 0 ? (
                        gainers.map((stock) => (
                            <div
                                className="market-mover market-cursor-pointer"
                                key={stock.symbol}
                                tabIndex={0}
                                role="button"
                                aria-selected={selectedSymbol === stock.symbol}
                                onClick={() => loadStockDetails(stock.symbol, selectedRange)}
                                onKeyDown={(e) => handleRowKeyDown(e, stock.symbol)}
                            >
                                <span><strong>{stock.symbol}</strong> - {stock.companyName}</span>
                                <span className="positive">{formatChangePercent(stock.changePercent)}</span>
                            </div>
                        ))
                    ) : (
                        <p className="empty-state">No market-mover data is currently available.</p>
                    )}
                </div>

                <div className="market-card">
                    <h2>Top Loss Companies</h2>
                    {error ? (
                        <p className="empty-state">{error}</p>
                    ) : pageLoading ? (
                        <p className="market-loading">Loading movers...</p>
                    ) : losers && losers.length > 0 ? (
                        losers.map((stock) => (
                            <div
                                className="market-mover market-cursor-pointer"
                                key={stock.symbol}
                                tabIndex={0}
                                role="button"
                                aria-selected={selectedSymbol === stock.symbol}
                                onClick={() => loadStockDetails(stock.symbol, selectedRange)}
                                onKeyDown={(e) => handleRowKeyDown(e, stock.symbol)}
                            >
                                <span><strong>{stock.symbol}</strong> - {stock.companyName}</span>
                                <span className="negative">{formatChangePercent(stock.changePercent)}</span>
                            </div>
                        ))
                    ) : (
                        <p className="empty-state">No market-mover data is currently available.</p>
                    )}
                </div>
            </section>
        </main>
    );
}