import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import MarketDataPage from '../pages/MarketDataPage';
import { MemoryRouter } from 'react-router-dom';

// Mock ResizeObserver for the SVG Chart component
class MockResizeObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
}
global.ResizeObserver = MockResizeObserver;

// Mock the API client calls
vi.mock('../api/market', () => ({
    searchStocks: vi.fn(),
    getCompanyDetails: vi.fn(),
    getCurrentPrice: vi.fn(),
    getPriceHistory: vi.fn(),
    getTopGainers: vi.fn(),
    getTopLosers: vi.fn()
}));

import {
    searchStocks,
    getCompanyDetails,
    getCurrentPrice,
    getPriceHistory,
    getTopGainers,
    getTopLosers
} from '../api/market';

describe('MarketDataPage Component Tests', () => {
    beforeEach(() => {
        vi.clearAllMocks();

        // Default mock setups
        getTopGainers.mockResolvedValue({ data: [
            { symbol: 'NVDA', companyName: 'NVIDIA Corporation', changePercent: 3.78 }
        ]});
        getTopLosers.mockResolvedValue({ data: [
            { symbol: 'TSLA', companyName: 'Tesla Inc.', changePercent: -2.11 }
        ]});

        getCompanyDetails.mockImplementation((symbol) => Promise.resolve({
            data: {
                symbol: symbol.toUpperCase(),
                companyName: symbol.toUpperCase() === 'AAPL' ? 'Apple Inc.' : 'Tesla Inc.',
                sector: 'Technology',
                industry: 'Electronics',
                description: `${symbol} mock description`,
                exchange: 'NASDAQ',
                currency: 'USD'
            }
        }));

        getCurrentPrice.mockImplementation((symbol) => Promise.resolve({
            data: {
                symbol: symbol.toUpperCase(),
                currentPrice: symbol.toUpperCase() === 'AAPL' ? 182.52 : 248.87,
                changeAmount: 2.24,
                changePercent: 1.24,
                lastUpdated: '2026-07-14T17:00:00'
            }
        }));

        getPriceHistory.mockImplementation((symbol) => Promise.resolve({
            data: [
                { symbol: symbol.toUpperCase(), date: '2026-07-14', openPrice: 180.00, highPrice: 183.00, lowPrice: 179.00, closePrice: 182.00, volume: 1000000 }
            ]
        }));
    });

    it('loads AAPL details by default on mount', async () => {
        render(
            <MemoryRouter initialEntries={['/market']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
            expect(screen.getByText('$182.52')).toBeInTheDocument();
        });
    });

    it('loads Tesla if symbol query param ?symbol=TSLA is in URL', async () => {
        render(
            <MemoryRouter initialEntries={['/market?symbol=TSLA']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Tesla Inc.')).toBeInTheDocument();
            expect(screen.getByText('$248.87')).toBeInTheDocument();
        });
    });

    it('performs search and automatically selects TSLA if single match is found', async () => {
        searchStocks.mockResolvedValue({
            data: [{ symbol: 'TSLA', companyName: 'Tesla Inc.', exchange: 'NASDAQ', currency: 'USD' }]
        });

        render(
            <MemoryRouter initialEntries={['/market']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        const input = screen.getByPlaceholderText('Search stock, e.g. apple');
        const searchBtn = screen.getByRole('button', { name: 'Search' });

        fireEvent.change(input, { target: { value: 'tesla' } });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(searchStocks).toHaveBeenCalledWith('tesla', 10);
            expect(screen.getByText('Tesla Inc.')).toBeInTheDocument();
        });
    });

    it('shows validation message on empty search query submission', async () => {
        render(
            <MemoryRouter initialEntries={['/market']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        const searchBtn = screen.getByRole('button', { name: 'Search' });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(screen.getByText('Enter a stock symbol or company name.')).toBeInTheDocument();
        });
    });

    it('shows no results message for unmatched searches', async () => {
        searchStocks.mockResolvedValue({ data: [] });

        render(
            <MemoryRouter initialEntries={['/market']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        const input = screen.getByPlaceholderText('Search stock, e.g. apple');
        const searchBtn = screen.getByRole('button', { name: 'Search' });

        fireEvent.change(input, { target: { value: 'xyz' } });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(screen.getByText('No stocks found.')).toBeInTheDocument();
        });
    });

    it('displays outage error retry message when backend request fails', async () => {
        getCompanyDetails.mockRejectedValueOnce(new Error('Network Error'));

        render(
            <MemoryRouter initialEntries={['/market']}>
                <MarketDataPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Market Data Service is unavailable.')).toBeInTheDocument();
            expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
        });
    });
});
