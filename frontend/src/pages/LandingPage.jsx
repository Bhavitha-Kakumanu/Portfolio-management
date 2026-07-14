import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

export default function LandingPage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="landing">
      <div className="hero">
        <div className="hero-content">
          <h1 className="hero-title">
            Investing for<br />
            <span className="hero-accent">Everyone</span>
          </h1>
          <p className="hero-subtitle">
            Commission-free investing, plus the tools you need to put your money in motion.
          </p>
          <div className="hero-actions">
            {isAuthenticated ? (
              <Link to="/dashboard" className="btn btn-green btn-lg">Go to Dashboard</Link>
            ) : (
              <>
                <Link to="/register" className="btn btn-green btn-lg">Get Started</Link>
                <Link to="/login" className="btn btn-outline btn-lg">Log in</Link>
              </>
            )}
          </div>
        </div>
        <div className="hero-visual">
          <div className="mock-card">
            <div className="mock-card-header">
              <span>Portfolio</span>
              <span className="mock-gain">+3.04%</span>
            </div>
            <div className="mock-value">$4,210.68</div>
            <svg viewBox="0 0 200 60" className="mini-chart">
              <path
                d="M0,50 C20,45 40,30 60,28 C80,26 100,38 120,25 C140,12 160,18 180,8 L200,5"
                fill="none"
                stroke="#00C805"
                strokeWidth="2"
              />
            </svg>
            <div className="mock-positions">
              {['AAPL', 'TSLA', 'NVDA', 'GOOGL'].map((s) => (
                <span key={s} className="mock-tag">{s}</span>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="features">
        <div className="feature">
          <div className="feature-icon">📈</div>
          <h3>Commission-Free Trading</h3>
          <p>Trade stocks and ETFs without paying a commission on every trade.</p>
        </div>
        <div className="feature">
          <div className="feature-icon">🔒</div>
          <h3>Secure & Reliable</h3>
          <p>JWT-secured accounts with BCrypt-hashed passwords and stateless auth.</p>
        </div>
        <div className="feature">
          <div className="feature-icon">⚡</div>
          <h3>Real-Time Data</h3>
          <p>Live market prices via the market-service (coming soon).</p>
        </div>
      </div>
    </div>
  );
}
