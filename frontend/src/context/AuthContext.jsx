import { createContext, useState } from 'react';

export const AuthContext = createContext(null);

function getInitialUser() {
  const token = localStorage.getItem('token');
  if (!token) {
    return null;
  }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      id: payload.sub,
      email: payload.email,
      role: payload.role,
    };
  } catch {
    localStorage.removeItem('token');
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser] = useState(getInitialUser);

  function signIn(authResponse) {
    localStorage.setItem('token', authResponse.token);
    setToken(authResponse.token);
    try {
      const payload = JSON.parse(atob(authResponse.token.split('.')[1]));
      setUser({
        id: payload.sub,
        email: payload.email,
        role: payload.role,
      });
    } catch {
      setUser({
        id: authResponse.userId,
        email: authResponse.email,
      });
    }
  }

  function logout() {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, token, signIn, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}
