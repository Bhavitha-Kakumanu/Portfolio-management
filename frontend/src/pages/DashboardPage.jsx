import { useEffect, useState } from 'react';
import { getMe } from '../api/users';
import { useAuth } from '../context/AuthContext';

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
