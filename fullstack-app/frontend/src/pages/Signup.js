import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';

/**
 * Signup Page Component
 *
 * ============================================================
 * WHAT DOES THIS PAGE DO?
 * ============================================================
 * This is the registration page where new users create accounts.
 * After successful signup, they're automatically logged in.
 *
 * ============================================================
 * REGISTRATION FLOW
 * ============================================================
 * 1. User fills out form (username, email, password)
 * 2. Client-side validation (passwords match)
 * 3. authService.signup() sends POST /api/auth/signup
 * 4. Backend creates user in database
 * 5. Backend returns JWT token (auto-login)
 * 6. User redirected to /dashboard
 *
 * ============================================================
 * REACT CONCEPTS USED
 * ============================================================
 * - useState with object: Single state for all form fields
 * - Spread operator: Updating one field while preserving others
 * - Client-side validation: Checking before API call
 * - Computed property names: [e.target.name] syntax
 */
function Signup() {
  // ============================================================
  // STATE
  // ============================================================

  /**
   * formData holds all form field values in one object.
   * This is cleaner than having 4 separate useState calls.
   */
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // ============================================================
  // EVENT HANDLERS
  // ============================================================

  /**
   * Handle input changes
   *
   * Uses computed property names to update the correct field:
   * [e.target.name]: e.target.value
   *
   * Example: If user types in the "email" field:
   * - e.target.name = "email"
   * - e.target.value = "user@example.com"
   * - Result: { ...formData, email: "user@example.com" }
   */
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Client-side validation
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    try {
      await authService.signup(
        formData.username,
        formData.email,
        formData.password
      );
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Signup failed. Please try again.');
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
        <h2 style={styles.title}>Create an account</h2>
        <p style={styles.subtitle}>Start building trading strategies today</p>

        {/* Error message */}
        {error && (
          <div style={styles.errorBox}>
            {error}
          </div>
        )}

        {/* Signup form */}
        <form onSubmit={handleSubmit}>

          {/* Username */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Username</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Choose a username"
              required
              disabled={loading}
              style={styles.input}
            />
          </div>

          {/* Email */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="you@example.com"
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
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="At least 6 characters"
              required
              disabled={loading}
              style={styles.input}
            />
          </div>

          {/* Confirm Password */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Re-enter your password"
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
            {loading ? 'Creating account...' : 'Create Account'}
          </button>

        </form>

        {/* Login link */}
        <p style={styles.linkText}>
          Already have an account?{' '}
          <Link to="/login" style={styles.link}>
            Sign in
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
    marginBottom: '18px',
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
    backgroundColor: '#22c55e',
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

export default Signup;
