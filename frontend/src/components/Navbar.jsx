import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <span className="brand-icon">🏦</span>
        <span className="brand-name">Robinhood</span>
      </Link>

      <div className="navbar-actions">
        {isAuthenticated ? (
          <>
            <span className="navbar-user">
              {user?.username || user?.email}
            </span>
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <button onClick={handleLogout} className="btn btn-outline btn-sm">
              Log out
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-link">Log in</Link>
            <Link to="/register" className="btn btn-green btn-sm">Sign up</Link>
          </>
        )}
      </div>
    </nav>
  );
}
