import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';

/**
 * Login Page Component
 *
 * ============================================================
 * WHAT DOES THIS PAGE DO?
 * ============================================================
 * This is the login page where users enter their credentials.
 * After successful login, they're redirected to the dashboard.
 *
 * ============================================================
 * AUTHENTICATION FLOW
 * ============================================================
 * 1. User enters username and password
 * 2. Form submits → handleSubmit() runs
 * 3. authService.login() sends POST /api/auth/login
 * 4. Backend validates credentials
 * 5. If valid, backend returns JWT token
 * 6. Token stored in localStorage
 * 7. User redirected to /dashboard
 *
 * ============================================================
 * REACT CONCEPTS USED
 * ============================================================
 * - useState: Managing form inputs and UI state
 * - useNavigate: Programmatic navigation after login
 * - Controlled inputs: Input values tied to React state
 * - async/await: Handling asynchronous API calls
 * - try/catch/finally: Error handling pattern
 */
function Login() {
  // ============================================================
  // STATE
  // ============================================================

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // ============================================================
  // FORM HANDLER
  // ============================================================

  /**
   * Handle login form submission
   *
   * @param {Event} e - Form submit event
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // ============================================================
  // RENDER
  // ============================================================

  return (
    <div style={styles.container}>
      <div style={styles.card}>

        {/* Logo */}
        <div style={styles.logoSection}>
          <h1 style={styles.logo}>StockDSL</h1>
          <p style={styles.tagline}>Backtest your trading strategies</p>
        </div>

        {/* Title */}
        <h2 style={styles.title}>Welcome back</h2>
        <p style={styles.subtitle}>Sign in to continue</p>

        {/* Error message */}
        {error && (
          <div style={styles.errorBox}>
            {error}
          </div>
        )}

        {/* Login form */}
        <form onSubmit={handleSubmit}>

          {/* Username */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              required
              disabled={loading}
              style={styles.input}
            />
          </div>

          {/* Password */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              disabled={loading}
              style={styles.input}
            />
          </div>

          {/* Submit button */}
          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.button,
              opacity: loading ? 0.7 : 1,
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

        </form>

        {/* Signup link */}
        <p style={styles.linkText}>
          Don't have an account?{' '}
          <Link to="/signup" style={styles.link}>
            Create one
          </Link>
        </p>

      </div>
    </div>
  );
}

// ============================================================
// STYLES
// ============================================================

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f5f7fa',
    padding: '20px',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: '16px',
    padding: '40px',
    width: '100%',
    maxWidth: '400px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
  },
  logoSection: {
    textAlign: 'center',
    marginBottom: '30px',
  },
  logo: {
    margin: '0 0 5px 0',
    fontSize: '32px',
    fontWeight: '700',
    color: '#1a1a1a',
  },
  tagline: {
    margin: 0,
    fontSize: '14px',
    color: '#6b7280',
  },
  title: {
    margin: '0 0 5px 0',
    fontSize: '24px',
    fontWeight: '600',
    color: '#1a1a1a',
  },
  subtitle: {
    margin: '0 0 25px 0',
    fontSize: '14px',
    color: '#6b7280',
  },
  errorBox: {
    padding: '12px 15px',
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    borderRadius: '8px',
    marginBottom: '20px',
    fontSize: '14px',
  },
  formGroup: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    marginBottom: '8px',
    fontWeight: '500',
    fontSize: '14px',
    color: '#374151',
  },
  input: {
    width: '100%',
    padding: '12px 15px',
    fontSize: '16px',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    backgroundColor: '#fff',
    boxSizing: 'border-box',
    transition: 'border-color 0.2s',
  },
  button: {
    width: '100%',
    padding: '14px',
    backgroundColor: '#3b82f6',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    marginTop: '10px',
  },
  linkText: {
    marginTop: '25px',
    textAlign: 'center',
    fontSize: '14px',
    color: '#6b7280',
  },
  link: {
    color: '#3b82f6',
    textDecoration: 'none',
    fontWeight: '500',
  },
};

export default Login;
