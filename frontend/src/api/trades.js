import axios from 'axios';

const tradeClient = axios.create({
  baseURL: 'http://localhost:8082',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const executeTrade = (tradeData) =>
  tradeClient.post('/api/trades', tradeData);

export const getTradeById = (id) =>
  tradeClient.get(`/api/trades/${id}`);

export const getTradesByUserId = (userId) =>
  tradeClient.get(`/api/trades/user/${userId}`);
export const getTradesByStockSymbol = (stockSymbol) =>
    tradeClient.get(`/api/trades/stock/${stockSymbol}`);