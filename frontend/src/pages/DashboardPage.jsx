import { useEffect, useState } from 'react';
import { getMe } from '../api/users';
import { useAuth } from '../context/useAuth';

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

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '$0.00';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
  };

  const formatPercentage = (val) => {
    if (val === undefined || val === null) return '0.00%';
    return new Intl.NumberFormat('en-US', {
      style: 'percent',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(val / 100);
  };

  const getGainLossPercentage = () => {
    if (!profile || !profile.portfolioValue) return 0;
    const costBasis = profile.portfolioValue - profile.totalGainLoss;
    return costBasis > 0 ? (profile.totalGainLoss / costBasis) * 100 : 0;
  };

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

      {profile && (
        <div className="stats-row" style={{ margin: '24px 0' }}>
          <div className="stat-card">
            <span className="stat-label">Total Portfolio Value</span>
            <span className="stat-value">{formatCurrency(profile.portfolioValue)}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Total Gain / Loss</span>
            <span className={`stat-value ${profile.totalGainLoss >= 0 ? 'positive' : 'negative'}`}>
              {profile.totalGainLoss >= 0 ? '+' : ''}{formatCurrency(profile.totalGainLoss)}
            </span>
            <span className={`stat-change ${profile.totalGainLoss >= 0 ? 'positive' : 'negative'}`}>
              {profile.totalGainLoss >= 0 ? '▲' : '▼'} {formatPercentage(Math.abs(getGainLossPercentage()))}
            </span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Invested Amount</span>
            <span className="stat-value">
              {formatCurrency(profile.investedAmount !== undefined && profile.investedAmount !== null
                ? profile.investedAmount
                : (profile.portfolioValue - profile.cashBalance))}
            </span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Cash Available</span>
            <span className="stat-value">{formatCurrency(profile.cashBalance)}</span>
          </div>
        </div>
      )}

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
