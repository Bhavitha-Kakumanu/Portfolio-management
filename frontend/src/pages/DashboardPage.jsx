import { useEffect, useState } from 'react';
import { getMe } from '../api/users';
import { useAuth } from '../context/useAuth';

function StatCard({ label, value, change, positive }) {
  return (
    <div className="stat-card">
      <span className="stat-label">{label}</span>
      <span className="stat-value">{value}</span>
      {change !== undefined && (
        <span className={`stat-change ${positive ? 'positive' : 'negative'}`}>
          {positive ? '+' : ''}{change}
        </span>
      )}
    </div>
  );
}

function PositionRow({ symbol, name, shares, price, totalValue, change, positive }) {
  return (
    <div className="position-row">
      <div className="position-symbol">
        <span className="symbol">{symbol}</span>
        <span className="name">{name}</span>
      </div>
      <div className="position-shares">{shares} shares</div>
      <div className="position-price">${price}</div>
      <div className={`position-change ${positive ? 'positive' : 'negative'}`}>
        {positive ? '+' : ''}{change}%
      </div>
      <div className="position-total">${totalValue}</div>
    </div>
  );
}

const MOCK_POSITIONS = [
  { symbol: 'AAPL', name: 'Apple Inc.', shares: 5, price: '182.52', totalValue: '912.60', change: '1.24', positive: true },
  { symbol: 'TSLA', name: 'Tesla, Inc.', shares: 2, price: '248.87', totalValue: '497.74', change: '-2.11', positive: false },
  { symbol: 'NVDA', name: 'NVIDIA Corp.', shares: 3, price: '875.40', totalValue: '2626.20', change: '3.47', positive: true },
  { symbol: 'GOOGL', name: 'Alphabet Inc.', shares: 1, price: '174.14', totalValue: '174.14', change: '0.62', positive: true },
];

export default function DashboardPage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(true);

  useEffect(() => {
    getMe()
      .then(({ data }) => setProfile(data))
      .catch(() => setProfile(null))
      .finally(() => setLoadingProfile(false));
  }, []);

  const displayName = profile
    ? `${profile.firstName} ${profile.lastName}`
    : user?.username || 'Investor';

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-greeting">Good morning, {displayName.split(' ')[0]}</h1>
          <p className="dashboard-date">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>
        {loadingProfile ? null : profile && (
          <div className="account-badge">
            <span className="badge-letter">{profile.firstName?.[0]}{profile.lastName?.[0]}</span>
            <div>
              <div className="badge-name">{profile.firstName} {profile.lastName}</div>
              <div className="badge-email">{profile.email}</div>
            </div>
          </div>
        )}
      </div>

      <div className="stats-row">
        <StatCard label="Portfolio Value" value="$4,210.68" change="$124.32 (3.04%)" positive={true} />
        <StatCard label="Today's Return" value="+$124.32" change="3.04%" positive={true} />
        <StatCard label="Total Return" value="+$710.68" change="20.32%" positive={true} />
        <StatCard label="Buying Power" value="$1,250.00" />
      </div>

      <div className="chart-section">
        <div className="chart-header">
          <h2>Portfolio Performance</h2>
          <div className="chart-periods">
            {['1D', '1W', '1M', '3M', '1Y', 'ALL'].map((p) => (
              <button key={p} className={`period-btn ${p === '1M' ? 'active' : ''}`}>{p}</button>
            ))}
          </div>
        </div>
        <div className="chart-placeholder">
          <svg viewBox="0 0 600 120" preserveAspectRatio="none" className="chart-svg">
            <defs>
              <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#00C805" stopOpacity="0.3" />
                <stop offset="100%" stopColor="#00C805" stopOpacity="0" />
              </linearGradient>
            </defs>
            <path
              d="M0,90 C30,85 60,70 90,65 C120,60 150,75 180,60 C210,45 240,30 270,35 C300,40 330,25 360,20 C390,15 420,30 450,22 C480,14 510,18 540,10 L600,8 L600,120 L0,120 Z"
              fill="url(#chartGrad)"
            />
            <path
              d="M0,90 C30,85 60,70 90,65 C120,60 150,75 180,60 C210,45 240,30 270,35 C300,40 330,25 360,20 C390,15 420,30 450,22 C480,14 510,18 540,10 L600,8"
              fill="none"
              stroke="#00C805"
              strokeWidth="2"
            />
          </svg>
          <p className="chart-note">Live chart will connect to market-service once built</p>
        </div>
      </div>

      <div className="positions-section">
        <div className="section-header">
          <h2>Your Positions</h2>
          <button className="btn btn-outline btn-sm">+ Add funds</button>
        </div>
        <div className="positions-table">
          <div className="positions-head">
            <span>Symbol</span>
            <span>Shares</span>
            <span>Price</span>
            <span>Change</span>
            <span>Value</span>
          </div>
          {MOCK_POSITIONS.map((p) => (
            <PositionRow key={p.symbol} {...p} />
          ))}
        </div>
        <p className="mock-note">Prices shown are mock data — market-service not yet implemented</p>
      </div>

      {profile && (
        <div className="profile-section">
          <h2>Account Details</h2>
          <div className="profile-grid">
            <div className="profile-item"><span>Username</span><strong>{profile.username}</strong></div>
            <div className="profile-item"><span>Email</span><strong>{profile.email}</strong></div>
            <div className="profile-item"><span>Member since</span><strong>{new Date(profile.createdAt).toLocaleDateString()}</strong></div>
            <div className="profile-item"><span>Account type</span><strong>{profile.role}</strong></div>
          </div>
        </div>
      )}
    </div>
  );
}
