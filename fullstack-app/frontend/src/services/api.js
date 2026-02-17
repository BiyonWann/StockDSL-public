import axios from 'axios';

/**
 * API Configuration Module
 *
 * This file sets up Axios (a popular HTTP client library) to communicate
 * with our Spring Boot backend.
 *
 * What this file does:
 * 1. Creates a pre-configured Axios instance with the backend URL
 * 2. Automatically adds JWT token to every request (authentication)
 * 3. Handles 401 errors globally (expired/invalid token)
 *
 * Why use Axios instead of fetch()?
 * - Automatic JSON transformation
 * - Request/response interceptors (middleware)
 * - Better error handling
 * - Simpler syntax
 *
 * Key concept: Interceptors
 * - Interceptors are like middleware - they run before/after every request
 * - Request interceptor: Adds JWT token to headers before sending
 * - Response interceptor: Handles errors after receiving response
 */

// ==========================================
// CONFIGURATION
// ==========================================

/**
 * Base URL for all API requests
 *
 * Our backend runs on port 8080, and all endpoints start with /api
 * Example endpoints:
 * - POST http://localhost:8080/api/auth/login
 * - GET  http://localhost:8080/api/strategies
 * - POST http://localhost:8080/api/backtests/run/1
 */
/**
 * API Base URL Configuration
 *
 * In development: Uses localhost:8080
 * In production: Set REACT_APP_API_URL environment variable
 *
 * How to set in production:
 * - Create .env.production file with: REACT_APP_API_URL=https://api.yourdomain.com/api
 * - Or set in AWS Amplify/S3 environment settings
 */
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Only log in development
if (process.env.NODE_ENV === 'development') {
  console.log('API Base URL:', API_URL);
}

// ==========================================
// CREATE AXIOS INSTANCE
// ==========================================

/**
 * Create a custom Axios instance
 *
 * axios.create() lets us set default configuration that applies to ALL requests
 * made with this instance.
 *
 * Configuration:
 * - baseURL: All requests will start with this URL
 *   Example: api.get('/strategies') → GET http://localhost:8080/api/strategies
 *
 * - headers: Default headers sent with every request
 *   'Content-Type': 'application/json' tells the server we're sending JSON data
 */
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==========================================
// REQUEST INTERCEPTOR
// ==========================================

/**
 * Request Interceptor - Runs BEFORE every request is sent
 *
 * Purpose: Automatically add JWT token to every API request
 *
 * How it works:
 * 1. Before any request is sent, this function runs
 * 2. We get the JWT token from localStorage
 * 3. If token exists, we add it to the Authorization header
 * 4. The request continues with the token attached
 *
 * Why is this useful?
 * - We don't have to manually add the token to every API call
 * - All authenticated requests are handled automatically
 *
 * Authorization header format:
 * "Bearer eyJhbGciOiJIUzI1NiJ9..."
 * - "Bearer" is the authentication scheme (standard for JWT)
 * - The token comes after a space
 */
api.interceptors.request.use(
  (config) => {
    // Get token from localStorage (stored when user logged in)
    const token = localStorage.getItem('token');

    // If we have a token, add it to the request headers
    if (token) {
      // Authorization header format: "Bearer <token>"
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Return the config so the request can continue
    return config;
  },
  (error) => {
    // If something goes wrong before the request is sent, reject it
    return Promise.reject(error);
  }
);

// ==========================================
// RESPONSE INTERCEPTOR
// ==========================================

/**
 * Response Interceptor - Runs AFTER every response is received
 *
 * Purpose: Handle common errors globally (especially 401 Unauthorized)
 *
 * How it works:
 * 1. If response is successful, just pass it through
 * 2. If response is an error, check the status code
 * 3. If 401 (Unauthorized), the token is invalid or expired
 * 4. Clear the invalid token and redirect to login page
 *
 * HTTP 401 Unauthorized means:
 * - Token is expired (JWT tokens have an expiration time)
 * - Token is invalid (tampered with or wrong format)
 * - Token is missing
 *
 * Why handle this globally?
 * - We don't have to check for 401 in every component
 * - User is automatically redirected to login when session expires
 */
api.interceptors.response.use(
  // Success handler: just pass the response through
  (response) => response,

  // Error handler: check for 401 and handle it
  (error) => {
    // Check if the error is a 401 Unauthorized
    if (error.response?.status === 401) {
      // Token is invalid or expired
      // Remove the bad token from localStorage
      localStorage.removeItem('token');

      // Redirect user to login page
      // They need to log in again to get a new token
      window.location.href = '/login';
    }

    // For all other errors, reject so the calling code can handle it
    return Promise.reject(error);
  }
);

// ==========================================
// EXPORT
// ==========================================

/**
 * Export the configured Axios instance
 *
 * Other files import this and use it to make API calls:
 *
 * Example usage in authService.js:
 *   import api from './api';
 *   const response = await api.post('/auth/login', { username, password });
 *
 * Example usage in strategyService.js:
 *   import api from './api';
 *   const response = await api.get('/strategies');
 */
export default api;
