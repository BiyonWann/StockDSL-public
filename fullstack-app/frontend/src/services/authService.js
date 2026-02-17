import api from './api';

/**
 * Authentication Service
 *
 * This service handles all authentication-related operations:
 * - User registration (signup)
 * - User login
 * - User logout
 * - Checking if user is logged in
 *
 * How authentication works in this app:
 * 1. User sends username/password to backend
 * 2. Backend validates credentials
 * 3. If valid, backend creates a JWT token and sends it back
 * 4. We store the token in localStorage
 * 5. All future API requests include this token (handled by api.js)
 * 6. Backend validates the token to identify the user
 *
 * What is JWT (JSON Web Token)?
 * - A token is a string that proves who you are
 * - It contains encoded user information (like user ID)
 * - It has an expiration time (e.g., 24 hours)
 * - The backend can verify if a token is valid
 *
 * Why use localStorage?
 * - Data persists even after browser is closed
 * - Token is available across page refreshes
 * - Simple to use: localStorage.setItem() and localStorage.getItem()
 */

const authService = {
  // ==========================================
  // SIGNUP (Create new account)
  // ==========================================

  /**
   * Register a new user
   *
   * Step by step:
   * 1. Send user data to backend: POST /api/auth/signup
   * 2. Backend creates user in database (password is hashed)
   * 3. Backend returns a JWT token
   * 4. We store the token in localStorage
   * 5. User is now logged in!
   *
   * @param {string} username - The username for the new account
   * @param {string} email - The user's email address
   * @param {string} password - The password (will be hashed by backend)
   * @returns {Promise<string>} The JWT token
   *
   * Example:
   *   const token = await authService.signup('john', 'john@email.com', 'secret123');
   */
  signup: async (username, email, password) => {
    // Send POST request to create new user
    const response = await api.post('/auth/signup', {
      username,
      email,
      password,
    });

    // Extract the token from response
    const { token } = response.data;

    // Store token in localStorage so user stays logged in
    localStorage.setItem('token', token);

    return token;
  },

  // ==========================================
  // LOGIN (Sign in to existing account)
  // ==========================================

  /**
   * Log in an existing user
   *
   * Step by step:
   * 1. Send credentials to backend: POST /api/auth/login
   * 2. Backend checks if username exists
   * 3. Backend verifies password using BCrypt
   * 4. If correct, backend returns a JWT token
   * 5. We store the token in localStorage
   * 6. User is now logged in!
   *
   * @param {string} username - The username
   * @param {string} password - The password
   * @returns {Promise<string>} The JWT token
   *
   * Example:
   *   const token = await authService.login('john', 'secret123');
   */
  login: async (username, password) => {
    // Send POST request to authenticate
    const response = await api.post('/auth/login', {
      username,
      password,
    });

    // Extract the token from response
    const { token } = response.data;

    // Store token in localStorage
    localStorage.setItem('token', token);

    return token;
  },

  // ==========================================
  // LOGOUT (Sign out)
  // ==========================================

  /**
   * Log out the current user
   *
   * How logout works:
   * 1. Remove the token from localStorage
   * 2. Redirect to login page
   *
   * Note: We don't need to tell the backend about logout.
   * JWT tokens are stateless - the backend doesn't track sessions.
   * Simply removing the token is enough because:
   * - Without a token, API requests will fail (401 Unauthorized)
   * - The user can't access protected pages
   */
  logout: () => {
    // Remove the token - user is no longer authenticated
    localStorage.removeItem('token');

    // Redirect to login page
    window.location.href = '/login';
  },

  // ==========================================
  // CHECK AUTHENTICATION STATUS
  // ==========================================

  /**
   * Check if the user is currently logged in
   *
   * How it works:
   * - If there's a token in localStorage, user is logged in
   * - If no token, user is not logged in
   *
   * Note: This doesn't verify if the token is valid/expired.
   * That's checked by the backend when we make API requests.
   *
   * @returns {boolean} True if user is logged in, false otherwise
   *
   * Example:
   *   if (authService.isAuthenticated()) {
   *     // Show dashboard
   *   } else {
   *     // Redirect to login
   *   }
   */
  isAuthenticated: () => {
    // Check if token exists in localStorage
    // Returns true if token exists, false if null
    return localStorage.getItem('token') !== null;
  },

  // ==========================================
  // GET CURRENT TOKEN
  // ==========================================

  /**
   * Get the current JWT token
   *
   * Useful when you need to manually add the token to a request
   * (though api.js does this automatically via interceptor)
   *
   * @returns {string|null} The token, or null if not logged in
   *
   * Example:
   *   const token = authService.getToken();
   *   console.log(token); // "eyJhbGciOiJIUzI1NiJ9..."
   */
  getToken: () => {
    return localStorage.getItem('token');
  },
};

export default authService;
