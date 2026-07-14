import client from './client';

export const getWatchlist = () => client.get('/api/v1/watchlist');
export const addWatchlist = (symbol) => client.post('/api/v1/watchlist', { symbol });
export const removeWatchlist = (symbol) => client.delete(`/api/v1/watchlist/${encodeURIComponent(symbol)}`);
export const favoriteWatchlist = (symbol, favorite) => client.post(`/api/v1/watchlist/${encodeURIComponent(symbol)}/favorite`, { favorite });
