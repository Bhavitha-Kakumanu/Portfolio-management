import { useEffect, useState } from 'react';
import { getWatchlist, addWatchlist, removeWatchlist, favoriteWatchlist } from '../api/watchlist';

export default function WatchlistPage() {
  const [symbol, setSymbol] = useState('');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const favoriteCount = items.filter((item) => item.favorite).length;

  useEffect(() => {
    loadWatchlist();
  }, []);

  async function loadWatchlist() {
    setLoading(true);
    setError('');
    try {
      const { data } = await getWatchlist();
      setItems(data);
    } catch (err) {
      setError(err.response?.data?.detail || 'Failed to load watchlist.');
    } finally {
      setLoading(false);
    }
  }

  async function handleAdd(e) {
    e.preventDefault();
    if (!symbol.trim()) return;
    setError('');
    try {
      await addWatchlist(symbol.trim().toUpperCase());
      setSymbol('');
      loadWatchlist();
    } catch (err) {
      setError(err.response?.data?.detail || 'Could not add stock.');
    }
  }

  async function handleRemove(symbolToRemove) {
    setError('');
    try {
      await removeWatchlist(symbolToRemove);
      setItems(items.filter((item) => item.symbol !== symbolToRemove));
    } catch (err) {
      setError(err.response?.data?.detail || 'Could not remove stock.');
    }
  }

  async function handleToggleFavorite(item) {
    setError('');
    try {
      const { data } = await favoriteWatchlist(item.symbol, !item.favorite);
      setItems((prev) => prev.map((it) => it.id === item.id ? { ...it, favorite: data.favorite } : it));
    } catch (err) {
      setError(err.response?.data?.detail || 'Could not update favorite.');
    }
  }

  return (
    <div className="watchlist-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Watchlist</h1>
          <p className="page-description">Track your favorite stocks and keep your portfolio organized.</p>
        </div>

        {items.length > 0 && (
          <div className="watchlist-stats">
            <div className="stat-card">
              <span className="stat-value">{items.length}</span>
              <span className="stat-label">Stocks tracked</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{favoriteCount}</span>
              <span className="stat-label">Favorites</span>
            </div>
          </div>
        )}
      </div>

      <section className="watchlist-actions">
        <form onSubmit={handleAdd} className="watchlist-form">
          <input
            value={symbol}
            onChange={(e) => setSymbol(e.target.value)}
            placeholder="Enter symbol, e.g. AAPL"
            aria-label="Stock symbol"
          />
          <button type="submit" className="btn btn-green btn-lg">Add</button>
        </form>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="watchlist-table">
        {loading ? (
          <div className="empty-state">Loading watchlist…</div>
        ) : items.length === 0 ? (
          <div className="empty-state">Your watchlist is empty. Add a stock symbol above.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Fav</th>
                <th>Symbol</th>
                <th>Added</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td>
                    <button
                      onClick={() => handleToggleFavorite(item)}
                      className={`btn btn-favorite ${item.favorite ? 'favorite' : ''}`}
                      aria-label="Toggle favorite"
                      aria-pressed={item.favorite}
                    >
                      {item.favorite ? '★' : '☆'}
                    </button>
                  </td>
                  <td>{item.symbol}</td>
                  <td>{new Date(item.createdAt).toLocaleDateString()}</td>
                  <td>
                    <button
                      className="btn btn-outline btn-sm btn-secondary"
                      onClick={() => handleRemove(item.symbol)}
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
