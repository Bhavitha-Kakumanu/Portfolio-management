# React Frontend — Complete Line-by-Line Explainer

Everything built for the frontend, explained in enough detail to teach it back.

---

## Table of Contents

1. [Project Setup](#1-project-setup)
2. [vite.config.js](#2-viteconfigjs)
3. [src/main.jsx](#3-srcmainjsx)
4. [src/index.css](#4-srcindexcss)
5. [src/api/client.js](#5-srcapiclientjs)
6. [src/api/auth.js](#6-srcapiauthjs)
7. [src/api/users.js](#7-srcapiusersjs)
8. [src/context/AuthContext.jsx](#8-srccontextauthcontextjsx)
9. [src/components/ProtectedRoute.jsx](#9-srccomponentsprotectedroutejsx)
10. [src/components/Navbar.jsx](#10-srccomponentsnavbarjsx)
11. [src/pages/LandingPage.jsx](#11-srcpageslandingpagejsx)
12. [src/pages/LoginPage.jsx](#12-srcpagesloginpagejsx)
13. [src/pages/RegisterPage.jsx](#13-srcpagesregisterpagejsx)
14. [src/pages/DashboardPage.jsx](#14-srcpagesdashboardpagejsx)
15. [src/App.jsx](#15-srcappjsx)
16. [How a Request Flows End-to-End](#16-how-a-request-flows-end-to-end)
17. [Folder Structure](#17-folder-structure)

---

## 1. Project Setup

### Command run
```
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install react-router-dom axios
```

**`npm create vite@latest frontend -- --template react`**
- `npm create vite@latest` downloads and runs the official Vite project scaffolder.
- `frontend` is the folder name it creates.
- `--template react` tells it to generate a bare React + JSX project (no TypeScript, no extra libraries).
- This gives you: `index.html`, `vite.config.js`, `src/main.jsx`, `src/App.jsx`, and a basic `src/index.css`.

**`npm install react-router-dom axios`**
- `react-router-dom` — the standard client-side routing library for React. It lets you define `<Route>` components that render different pages based on the URL, without ever asking the server for a new HTML page.
- `axios` — an HTTP client library. It wraps the browser's `fetch` API with a cleaner interface, interceptor support (middleware for every request/response), and automatic JSON parsing.

---

## 2. vite.config.js

```js
import { defineConfig } from 'vite'        // line 1
import react from '@vitejs/plugin-react'   // line 2
                                            // line 3
export default defineConfig({              // line 4
  plugins: [react()],                      // line 5
  server: {                                // line 6
    port: 3000,                            // line 7
    proxy: {                               // line 8
      '/api': {                            // line 9
        target: 'http://localhost:8081',   // line 10
        changeOrigin: true,                // line 11
      },                                   // line 12
    },                                     // line 13
  },                                       // line 14
})                                         // line 15
```

**Line 1** — `defineConfig` is a helper from Vite that gives you TypeScript autocompletion on the config object. At runtime it just returns what you pass in.

**Line 2** — `@vitejs/plugin-react` enables JSX transformation and React Fast Refresh (hot module replacement — the page updates instantly when you save a file without losing component state).

**Line 5** — `plugins: [react()]` registers the React plugin with Vite.

**Line 7** — `port: 3000` tells the dev server to listen on port 3000 instead of Vite's default 5173.

**Lines 8–12** — The `proxy` block solves CORS. During development, when your React app (running on port 3000) makes a fetch to `/api/v1/auth/login`, the browser would normally block it because it's a cross-origin request to `localhost:8081`. The proxy intercepts any request starting with `/api` and forwards it to `http://localhost:8081`. From the browser's perspective, the request goes to port 3000 (same origin), so CORS is never triggered.

**Line 11** — `changeOrigin: true` rewrites the `Host` header in the forwarded request to match the target host. Some servers reject requests where `Host` doesn't match their own address.

---

## 3. src/main.jsx

```jsx
import { StrictMode } from 'react'                          // line 1
import { createRoot } from 'react-dom/client'              // line 2
import './index.css'                                        // line 3
import App from './App.jsx'                                 // line 4
                                                            // line 5
createRoot(document.getElementById('root')).render(        // line 6
  <StrictMode>                                             // line 7
    <App />                                                // line 8
  </StrictMode>,                                           // line 9
)                                                          // line 10
```

This file was not changed — it's the original Vite scaffold. Explained here for completeness.

**Line 1** — `StrictMode` is a React development tool. It renders every component twice (in development only) to help catch side effects that run at the wrong time. Has no effect in production builds.

**Line 2** — `createRoot` is the React 18 API for mounting an app. The older `ReactDOM.render` is deprecated.

**Line 3** — Importing a CSS file in JavaScript is a Vite feature. Vite reads the import, injects the styles into the page at runtime, and hot-reloads them when you save.

**Line 6** — `document.getElementById('root')` finds the single `<div id="root">` in `index.html`. React renders the entire application inside that one div. Everything else (routing, pages, components) is handled in JavaScript — the browser never navigates to a new HTML file.

---

## 4. src/index.css

### CSS custom properties (design tokens)

```css
:root {
  --bg:          #0a0a0a;   /* near-black page background */
  --bg-card:     #141414;   /* slightly lighter for cards */
  --bg-input:    #1a1a1a;   /* input fields and hover states */
  --border:      #2a2a2a;   /* subtle borders between elements */
  --text:        #e8e8e8;   /* primary readable text */
  --text-muted:  #8a8a8a;   /* secondary/helper text */
  --green:       #00c805;   /* Robinhood's signature green */
  --green-dark:  #009c04;   /* darker green for hover states */
  --red:         #ff5000;   /* losses and error states */
}
```

`:root` is the CSS pseudo-class that matches the top-level `<html>` element. Variables defined here are available everywhere in the stylesheet via `var(--name)`. This is called a design token system — change `--green` in one place and it updates every button, chart line, and badge throughout the app.

### Reset

```css
* { box-sizing: border-box; margin: 0; padding: 0; }
```

`box-sizing: border-box` makes width/height include padding and border, not add on top of them. Without this, a `width: 200px` element with `padding: 16px` would actually be 232px wide. `margin: 0; padding: 0` removes browser default spacing so we start from a clean baseline.

### Navbar styles

```css
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
}
```

`position: sticky` + `top: 0` keeps the navbar pinned to the top of the viewport as the user scrolls. `z-index: 100` ensures it layers on top of all page content.

### Button system

```css
.btn { border-radius: 24px; transition: opacity 0.15s; }
.btn-green  { background: var(--green); color: #000; }
.btn-outline { background: transparent; border: 1px solid var(--border); }
.btn-sm  { padding: 6px 16px; font-size: 13px; }
.btn-lg  { padding: 14px 28px; font-size: 16px; }
.btn-full { width: 100%; padding: 14px; }
```

Buttons use a modifier class pattern: `.btn` is the base, `.btn-green` sets the color variant, `.btn-sm`/`.btn-lg`/`.btn-full` set the size. You compose them: `className="btn btn-green btn-sm"`. `border-radius: 24px` makes them pill-shaped, matching Robinhood's style.

### Auth form layout

```css
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 56px);
}
```

`min-height: calc(100vh - 56px)` makes the auth page fill the full viewport height minus the 56px navbar. `align-items: center` + `justify-content: center` centers the card both vertically and horizontally.

### Responsive grid

```css
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
```

Used for the first name / last name fields in registration. `1fr 1fr` gives each field exactly half the available width. `fr` is a fractional unit — it divides the available space equally.

### Stats grid with responsive breakpoint

```css
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}
@media (max-width: 700px) { .stats-row { grid-template-columns: 1fr 1fr; } }
```

Four equal columns on desktop, two columns on mobile. `repeat(4, 1fr)` is shorthand for `1fr 1fr 1fr 1fr`.

---

## 5. src/api/client.js

```js
import axios from 'axios';                                           // line 1
                                                                     // line 2
const client = axios.create({                                        // line 3
  baseURL: '',                                                        // line 4
  headers: { 'Content-Type': 'application/json' },                  // line 5
});                                                                   // line 6
                                                                     // line 7
client.interceptors.request.use((config) => {                       // line 8
  const token = localStorage.getItem('token');                      // line 9
  if (token) config.headers.Authorization = `Bearer ${token}`;     // line 10
  return config;                                                     // line 11
});                                                                   // line 12
                                                                     // line 13
client.interceptors.response.use(                                   // line 14
  (res) => res,                                                      // line 15
  (err) => {                                                         // line 16
    if (err.response?.status === 401) {                             // line 17
      localStorage.removeItem('token');                             // line 18
      window.location.href = '/login';                              // line 19
    }                                                               // line 20
    return Promise.reject(err);                                     // line 21
  }                                                                  // line 22
);                                                                   // line 23
                                                                     // line 24
export default client;                                               // line 25
```

**Line 3** — `axios.create()` creates a new Axios instance with pre-configured defaults. This is better than using `axios` directly because all API calls share the same base config without repetition.

**Line 4** — `baseURL: ''` means all URLs are relative (e.g., `/api/v1/auth/login`). The Vite proxy then forwards `/api/*` to `localhost:8081`. In production you'd set this to the actual API domain.

**Line 5** — `'Content-Type': 'application/json'` tells the Spring backend that the request body is JSON. Without this header, Spring's `@RequestBody` parsing would fail.

**Lines 8–11** — A request interceptor runs before every outgoing request. It reads the JWT from `localStorage` and appends it as the `Authorization` header. The backend's `JwtAuthFilter` reads this header to authenticate the request. Without it, protected endpoints return 401.

**Line 10** — Template literal `` `Bearer ${token}` `` produces `"Bearer eyJ..."`. The "Bearer" prefix is a convention defined in RFC 6750 for how to send OAuth tokens.

**Lines 14–22** — A response interceptor runs after every incoming response. The first argument handles success (just passes the response through). The second handles errors.

**Line 17** — `?.` is optional chaining. If `err.response` is `undefined` (e.g. network timeout — no response at all), `err.response?.status` returns `undefined` instead of throwing a TypeError.

**Lines 18–19** — If the backend returns 401 (token expired or invalid), clear the stored token and hard-navigate to `/login`. `window.location.href` does a full page navigation (not a React Router navigation) — this guarantees the React app's entire state is reset, not just the route.

**Line 21** — `return Promise.reject(err)` re-throws the error so the calling code (e.g., `LoginPage`) can still catch it and show an error message.

---

## 6. src/api/auth.js

```js
import client from './client';                                         // line 1
                                                                       // line 2
export const register = (data) => client.post('/api/v1/auth/register', data); // line 3
export const login    = (data) => client.post('/api/v1/auth/login',    data); // line 4
```

**Lines 3–4** — Each function maps directly to one backend endpoint:
- `register` → `POST /api/v1/auth/register` (AuthController.register)
- `login`    → `POST /api/v1/auth/login`    (AuthController.login)

`client.post(url, data)` serializes `data` to JSON (because of the `Content-Type` header set in client.js) and returns a Promise that resolves to the Axios response object. The response body is in `.data`.

---

## 7. src/api/users.js

```js
import client from './client';                                             // line 1
                                                                           // line 2
export const getMe        = ()   => client.get('/api/v1/users/me');       // line 3
export const getUserById  = (id) => client.get(`/api/v1/users/${id}`);   // line 4
```

**Line 3** — `GET /api/v1/users/me` hits `UserController.getMe()`. The backend reads the user ID from the JWT (via `@AuthenticationPrincipal`) — no need to pass the ID in the URL.

**Line 4** — `GET /api/v1/users/{id}` hits `UserController.getUserById()`. The `id` is interpolated into the URL string.

Both calls automatically include the JWT via the request interceptor in `client.js`.

---

## 8. src/context/AuthContext.jsx

```jsx
import { createContext, useContext, useState, useEffect } from 'react'; // line 1
                                                                         // line 2
const AuthContext = createContext(null);                                 // line 3
                                                                         // line 4
export function AuthProvider({ children }) {                            // line 5
  const [user, setUser]   = useState(null);                             // line 6
  const [token, setToken] = useState(() => localStorage.getItem('token')); // line 7
                                                                         // line 8
  useEffect(() => {                                                      // line 9
    if (token) {                                                         // line 10
      try {                                                              // line 11
        const payload = JSON.parse(atob(token.split('.')[1]));          // line 12
        setUser({ id: payload.sub, email: payload.email, role: payload.role }); // line 13
      } catch {                                                          // line 14
        logout();                                                        // line 15
      }                                                                  // line 16
    }                                                                    // line 17
  }, [token]);                                                           // line 18
                                                                         // line 19
  function signIn(authResponse) {                                       // line 20
    localStorage.setItem('token', authResponse.token);                 // line 21
    setToken(authResponse.token);                                       // line 22
    setUser({                                                           // line 23
      id:       authResponse.userId,                                    // line 24
      username: authResponse.username,                                  // line 25
      email:    authResponse.email,                                     // line 26
    });                                                                  // line 27
  }                                                                      // line 28
                                                                         // line 29
  function logout() {                                                   // line 30
    localStorage.removeItem('token');                                   // line 31
    setToken(null);                                                      // line 32
    setUser(null);                                                       // line 33
  }                                                                      // line 34
                                                                         // line 35
  return (                                                               // line 36
    <AuthContext.Provider value={{ user, token, signIn, logout, isAuthenticated: !!token }}> // line 37
      {children}                                                        // line 38
    </AuthContext.Provider>                                             // line 39
  );                                                                     // line 40
}                                                                        // line 41
                                                                         // line 42
export const useAuth = () => useContext(AuthContext);                   // line 43
```

**Line 3** — `createContext(null)` creates a React Context object. Context is a way to share state across the entire component tree without passing props down manually at every level ("prop drilling"). `null` is the default value — used if a component tries to read the context outside a `<AuthProvider>`.

**Line 5** — `AuthProvider` is a wrapper component. Every component inside it can access the auth state.

**Line 6** — `useState(null)` stores the current user object (`{ id, username, email }`). Starts as `null` (not logged in).

**Line 7** — `useState(() => localStorage.getItem('token'))` — the function form of `useState` is called a "lazy initializer." It runs only once on mount, not on every render. This reads the token from `localStorage` so the user stays logged in across page refreshes.

**Lines 9–18** — `useEffect` runs after the component renders. When `token` changes (login, logout, or page load), this effect decodes the JWT payload to populate the `user` state.

**Line 12** — A JWT is three base64-encoded segments separated by dots: `header.payload.signature`. `token.split('.')[1]` takes the middle segment (the payload). `atob()` is the browser's built-in base64 decoder. `JSON.parse()` turns the decoded string into an object. The JWT payload contains `sub` (the user's UUID), `email`, and `role` — claims the backend embedded when it generated the token.

**Line 14–15** — If the token is malformed and `atob`/`JSON.parse` throws, we call `logout()` to wipe the corrupted token rather than leaving the app in a broken state.

**Lines 20–27** — `signIn()` is called by `LoginPage` and `RegisterPage` after a successful API response. It saves the token to `localStorage` (persists across refreshes) and updates both `token` and `user` state (triggers a re-render throughout the app).

**Lines 30–33** — `logout()` clears the token from `localStorage` and resets both state values to `null`, which will cause `isAuthenticated` to become `false` and re-render anything that reads auth state.

**Line 37** — `isAuthenticated: !!token` — `!!` converts any truthy/falsy value to a strict boolean. If `token` is a non-empty string, `!!token` is `true`. If `token` is `null`, `!!token` is `false`.

**Line 43** — `useAuth` is a custom hook. Instead of importing both `useContext` and `AuthContext` in every component, you just import `useAuth` and call it. This is idiomatic React.

---

## 9. src/components/ProtectedRoute.jsx

```jsx
import { Navigate } from 'react-router-dom';   // line 1
import { useAuth } from '../context/AuthContext'; // line 2
                                                  // line 3
export default function ProtectedRoute({ children }) { // line 4
  const { isAuthenticated } = useAuth();         // line 5
  return isAuthenticated ? children : <Navigate to="/login" replace />; // line 6
}
```

**Line 4** — `{ children }` is a special React prop. When you wrap a component like `<ProtectedRoute><Dashboard /></ProtectedRoute>`, `children` is the `<Dashboard />` element.

**Line 5** — Reads `isAuthenticated` from the auth context.

**Line 6** — A ternary: if the user has a valid token, render the wrapped page (`children`). Otherwise render `<Navigate to="/login" replace />`.

`<Navigate>` is a React Router component that triggers a redirect. `replace` means it replaces the current entry in the browser history instead of pushing a new one — so hitting the back button doesn't bounce you back to the protected page you just got redirected from.

---

## 10. src/components/Navbar.jsx

```jsx
import { Link, useNavigate } from 'react-router-dom';    // line 1
import { useAuth } from '../context/AuthContext';          // line 2
                                                           // line 3
export default function Navbar() {                        // line 4
  const { isAuthenticated, user, logout } = useAuth();   // line 5
  const navigate = useNavigate();                         // line 6
                                                           // line 7
  function handleLogout() {                               // line 8
    logout();                                             // line 9
    navigate('/login');                                   // line 10
  }                                                        // line 11
```

**Line 1** — `Link` is React Router's replacement for `<a href>`. It intercepts the click event and uses the History API to update the URL without a full page reload, which keeps React state alive.

**Line 6** — `useNavigate()` returns a function for programmatic navigation (from inside event handlers). Used after logout to redirect to `/login`.

**Lines 8–10** — `logout()` clears auth state, then `navigate('/login')` updates the URL. Both happen synchronously in the same event handler before React re-renders.

```jsx
  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand"> ...
      </Link>
      <div className="navbar-actions">
        {isAuthenticated ? (
          // shows: username, Dashboard link, Log out button
        ) : (
          // shows: Log in link, Sign up button
        )}
      </div>
    </nav>
  );
```

The navbar renders different content based on `isAuthenticated`. When auth state changes (login/logout), React automatically re-renders the navbar because it reads from the context.

---

## 11. src/pages/LandingPage.jsx

The landing page has two sections: a hero and a features row.

```jsx
export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  // ...
  {isAuthenticated ? (
    <Link to="/dashboard" className="btn btn-green btn-lg">Go to Dashboard</Link>
  ) : (
    <>
      <Link to="/register" className="btn btn-green btn-lg">Get Started</Link>
      <Link to="/login" className="btn btn-outline btn-lg">Log in</Link>
    </>
  )}
```

If already logged in, the CTA sends you to the dashboard instead of register/login.

```jsx
<svg viewBox="0 0 200 60" className="mini-chart">
  <path
    d="M0,50 C20,45 40,30 60,28 ..."
    fill="none"
    stroke="#00C805"
    strokeWidth="2"
  />
</svg>
```

The chart in the hero card is a raw SVG path — no chart library needed for a decorative element. `C` commands in the `d` attribute are cubic Bézier curves, which produce the smooth upward-trending line.

---

## 12. src/pages/LoginPage.jsx

```jsx
const [form, setForm] = useState({ email: '', password: '' });  // line 1
const [error, setError] = useState('');                          // line 2
const [loading, setLoading] = useState(false);                  // line 3
```

**Line 1** — One state object holds all form field values. This is called "controlled components" — React state is the single source of truth for what's in the inputs.

**Line 2** — Separate state for the API error message (e.g., "Invalid email or password").

**Line 3** — `loading` disables the submit button while the API call is in flight, preventing double-submits.

```jsx
function handleChange(e) {
  setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
}
```

`[e.target.name]` is a computed property key — it uses the input's `name` attribute as the key. The `name="email"` input updates `form.email`, `name="password"` updates `form.password`. The `...f` spread copies all existing fields before overwriting just the one that changed.

```jsx
async function handleSubmit(e) {
  e.preventDefault();   // prevent the browser's default form submission (page reload)
  setError('');
  setLoading(true);
  try {
    const { data } = await login(form);  // POST /api/v1/auth/login
    signIn(data);                         // save token, update auth context
    navigate('/dashboard');              // redirect on success
  } catch (err) {
    setError(
      err.response?.data?.detail || 'Invalid email or password.'
    );
  } finally {
    setLoading(false);   // always re-enable the button, success or failure
  }
}
```

`e.preventDefault()` — forms normally submit via HTTP, which reloads the page. We handle submission in JavaScript instead.

`const { data } = await login(form)` — destructures the Axios response. The actual response body is in `.data`.

`err.response?.data?.detail` — the Spring `GlobalExceptionHandler` returns `ProblemDetail` JSON with a `detail` field (e.g. `"Bad credentials"`). The `?.` chain guards against network errors where there's no response body at all.

```jsx
<button type="submit" className="btn btn-green btn-full" disabled={loading}>
  {loading ? 'Logging in…' : 'Log in'}
</button>
```

`disabled={loading}` prevents double-clicks. The button label changes to "Logging in…" for visual feedback.

---

## 13. src/pages/RegisterPage.jsx

The register page is similar to login, with two key additions:

**Field-level validation errors:**
```jsx
const [errors, setErrors] = useState({});

// Inside catch:
if (err.response?.data?.errors) {
  setErrors(err.response.data.errors);
}
```

The Spring backend returns validation failures as a map of field names to error messages:
```json
{ "errors": { "email": "must be a valid email", "password": "size must be >= 8" } }
```
The frontend stores this map in `errors` state and displays each message under its field:

```jsx
{errors.username && <span className="field-error">{errors.username}</span>}
```

**Clearing a field error on change:**
```jsx
function handleChange(e) {
  setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  setErrors((prev) => ({ ...prev, [e.target.name]: '' }));  // clear this field's error
}
```

When the user starts typing in a field that had an error, that error disappears immediately, giving responsive feedback.

**First name / last name side by side:**
```jsx
<div className="field-row">
  <div className="field">...</div>  {/* firstName */}
  <div className="field">...</div>  {/* lastName */}
</div>
```

`.field-row` uses `grid-template-columns: 1fr 1fr` to place them side by side.

---

## 14. src/pages/DashboardPage.jsx

### Fetching the real user profile

```jsx
useEffect(() => {
  getMe()
    .then(({ data }) => setProfile(data))
    .catch(() => setProfile(null))
    .finally(() => setLoadingProfile(false));
}, []);
```

`useEffect` with an empty dependency array `[]` runs once after the component first mounts — equivalent to "on load." It calls `GET /api/v1/users/me`, which hits the Spring `UserController.getMe()` endpoint. The JWT is automatically attached by the Axios interceptor in `client.js`.

`.catch(() => setProfile(null))` — if the call fails (e.g. backend is down), `profile` stays `null` and the page still renders gracefully using auth context data as a fallback.

### StatCard component

```jsx
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
```

A small presentational component. `change !== undefined` means the change row is optional — the "Buying Power" card has no change value. `` `stat-change ${positive ? 'positive' : 'negative'}` `` dynamically applies either the `.positive` (green) or `.negative` (red) CSS class.

### SVG chart

```jsx
<svg viewBox="0 0 600 120" preserveAspectRatio="none" className="chart-svg">
  <defs>
    <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stopColor="#00C805" stopOpacity="0.3" />
      <stop offset="100%" stopColor="#00C805" stopOpacity="0" />
    </linearGradient>
  </defs>
  <path d="M0,90 C30,85 ..." fill="url(#chartGrad)" />  {/* filled area */}
  <path d="M0,90 C30,85 ..." fill="none" stroke="#00C805" strokeWidth="2" /> {/* line */}
</svg>
```

Two overlapping SVG paths create the Robinhood-style chart. The first path is a filled shape (line + area below it), colored with a gradient that fades from green to transparent. The second path draws just the green line on top. `preserveAspectRatio="none"` lets the chart stretch to any container width without maintaining aspect ratio.

### Mock positions

```js
const MOCK_POSITIONS = [
  { symbol: 'AAPL', name: 'Apple Inc.', shares: 5, price: '182.52', ... },
  ...
];
```

Hardcoded data. The real market-service (not yet built) would replace this with live prices from an API. The `PositionRow` component renders one row per position in a CSS grid table layout.

### Account details section

```jsx
{profile && (
  <div className="profile-section">
    <div className="profile-grid">
      <div className="profile-item"><span>Username</span><strong>{profile.username}</strong></div>
      ...
    </div>
  </div>
)}
```

`{profile && (...)}` — conditional rendering. The section only appears once the `getMe()` call resolves. `profile` is `null` initially, so React skips rendering this block until data arrives.

---

## 15. src/App.jsx

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
// ... page imports

export default function App() {
  return (
    <AuthProvider>                          {/* makes auth state available everywhere */}
      <BrowserRouter>                       {/* enables URL-based routing */}
        <div className="app-shell">
          <Navbar />                        {/* always visible */}
          <main className="main-content">
            <Routes>
              <Route path="/"          element={<LandingPage />} />
              <Route path="/login"     element={<LoginPage />} />
              <Route path="/register"  element={<RegisterPage />} />
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>        {/* wraps dashboard in auth check */}
                    <DashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route path="*" element={<Navigate to="/" replace />} /> {/* 404 fallback */}
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
```

**`<AuthProvider>`** wraps everything so every component in the tree can call `useAuth()`.

**`<BrowserRouter>`** connects React Router to the browser's History API. It listens for URL changes and re-renders the matching `<Route>`.

**`<Routes>`** is the router outlet. It looks at the current URL and renders the first `<Route>` whose `path` matches.

**`<Route path="*">`** is the catch-all (equivalent to a 404 handler). Any URL that doesn't match the routes above redirects to `/`.

**Provider ordering matters:** `AuthProvider` is outside `BrowserRouter` so that if auth state ever needed to trigger a redirect, the router context would be available via the `useNavigate` hook inside components. In practice either order works here, but outside-in is the conventional pattern.

---

## 16. How a Request Flows End-to-End

### Registration flow

```
User fills form → clicks "Sign up"
  │
  ▼
RegisterPage.handleSubmit()
  └─ e.preventDefault()         ← stops browser form submission
  └─ setLoading(true)           ← disables button
  └─ register(form)             ← calls POST /api/v1/auth/register
        │
        ▼
     client.js request interceptor
        └─ no token in localStorage yet, skips Authorization header
        │
        ▼
     Vite proxy (/api/* → localhost:8081)
        │
        ▼
     Spring AuthController.register()
        └─ validates fields (@Valid)
        └─ hashes password (BCrypt)
        └─ saves user to Postgres
        └─ generates JWT
        └─ returns AuthResponse { token, userId, username, email }
        │
        ▼
     client.js response interceptor
        └─ status 201 → passes through
        │
        ▼
  RegisterPage receives { data: { token, userId, username, email } }
  └─ signIn(data)               ← saves token to localStorage, sets user state
  └─ navigate('/dashboard')     ← URL changes to /dashboard
        │
        ▼
  App.jsx Routes renders <ProtectedRoute>
  └─ isAuthenticated = true     ← renders <DashboardPage>
```

### Protected page load flow

```
User visits /dashboard (token already in localStorage)
  │
  ▼
main.jsx renders <App>
  └─ AuthProvider initializes: reads token from localStorage
  └─ useEffect decodes JWT payload → sets user state
  │
  ▼
<ProtectedRoute> checks isAuthenticated = true → renders <DashboardPage>
  │
  ▼
DashboardPage mounts
  └─ useEffect → getMe() → GET /api/v1/users/me
        │
        ▼
     client.js request interceptor
        └─ reads token from localStorage
        └─ adds Authorization: Bearer <token>
        │
        ▼
     Spring JwtAuthFilter
        └─ validates token signature
        └─ extracts userId from JWT
        └─ stores userId in SecurityContext
        │
        ▼
     Spring UserController.getMe()
        └─ reads userId from SecurityContext (never from request body)
        └─ SELECT * FROM users WHERE id = ?
        └─ returns UserResponse (no passwordHash)
        │
        ▼
  DashboardPage: setProfile(data) → renders account details
```

### Token expiry flow

```
User with expired JWT visits /dashboard
  │
  ▼
DashboardPage mounts → getMe() → GET /api/v1/users/me
  │
  ▼
Spring returns 401 Unauthorized
  │
  ▼
client.js response interceptor
  └─ sees status 401
  └─ localStorage.removeItem('token')
  └─ window.location.href = '/login'   ← full page reload to /login
```

---

## 17. Folder Structure

```
frontend/
├── vite.config.js               ← build tool config + dev proxy
├── package.json                 ← dependencies
└── src/
    ├── main.jsx                 ← app entry point, mounts React into index.html
    ├── App.jsx                  ← router + layout shell
    ├── index.css                ← global styles + design tokens
    │
    ├── api/                     ← all HTTP calls, isolated from UI logic
    │   ├── client.js            ← configured Axios instance + interceptors
    │   ├── auth.js              ← register, login
    │   └── users.js             ← getMe, getUserById
    │
    ├── context/
    │   └── AuthContext.jsx      ← JWT state, signIn, logout, isAuthenticated
    │
    ├── components/              ← reusable UI pieces
    │   ├── Navbar.jsx           ← top nav, auth-aware
    │   └── ProtectedRoute.jsx   ← redirects unauthenticated users
    │
    └── pages/                   ← one file per route
        ├── LandingPage.jsx      ← /
        ├── LoginPage.jsx        ← /login
        ├── RegisterPage.jsx     ← /register
        └── DashboardPage.jsx    ← /dashboard (protected)
```

### Why this structure?

- **`api/`** — separating HTTP calls from components means you can change the API URL or swap Axios for `fetch` in one place, without touching any page component.
- **`context/`** — auth state is global (needed by Navbar, ProtectedRoute, and pages). Context avoids prop drilling the token down through every component in the tree.
- **`components/`** — reusable pieces that appear across multiple pages (Navbar, ProtectedRoute) live here, separate from page-level components.
- **`pages/`** — one component per route. Each page is responsible for its own data fetching, form state, and layout.
