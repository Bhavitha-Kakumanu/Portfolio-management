import axios from 'axios';

const MARKET_API_URL = import.meta.env.VITE_MARKET_API_URL || 'http://localhost:8083/api/v1/market';

const marketClient = axios.create({
  baseURL: MARKET_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const searchStocks = (query, limit = 10) =>
    marketClient.get('/search', { params: { query, limit } });

export const getCompanyDetails = (symbol) =>
    marketClient.get(`/company/${symbol}`);

export const getCurrentPrice = (symbol) =>
    marketClient.get(`/price/${symbol}`);

export const getPriceHistory = (symbol, range = 'ALL') =>
    marketClient.get(`/history/${symbol}`, { params: { range } });

export const getTopGainers = () =>
    marketClient.get('/top-gainers');

export const getTopLosers = () =>
    marketClient.get('/top-losers');