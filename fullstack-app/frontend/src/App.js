import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import StrategyEditor from './pages/StrategyEditor';
import authService from './services/authService';
import './styles/App.css';

/**
 * Main App Component - The Root of the Application
 *
 * ============================================================
 * WHAT IS THIS FILE?
 * ============================================================
 * This is the top-level React component. Everything in the app
 * starts here. It sets up:
 * 1. React Router (for navigation between pages)
 * 2. Protected routes (pages that require login)
 * 3. Public routes (pages anyone can see)
 *
 * ============================================================
 * WHAT IS REACT ROUTER?
 * ============================================================
 * React Router is a library for navigation in React apps.
 *
 * Without React Router:
 * - Every "page" would need a separate HTML file
 * - Clicking a link would reload the entire page
 * - Slow and not a good user experience
 *
 * With React Router:
 * - All pages are React components
 * - Clicking a link only re-renders the changed parts
 * - Fast and smooth (called a "Single Page Application" or SPA)
 *
 * Key components:
 * - <Router>: Wraps the entire app, enables routing
 * - <Routes>: Container for all route definitions
 * - <Route>: Defines a path → component mapping
 * - <Navigate>: Redirects to another route
 *
 * ============================================================
 * URL PATTERNS
 * ============================================================
 *
 * | URL              | Component       | Protected? | Description         |
 * |------------------|-----------------|------------|---------------------|
 * | /login           | Login           | No         | Login form          |
 * | /signup          | Signup          | No         | Registration form   |
 * | /dashboard       | Dashboard       | Yes        | View all strategies |
 * | /strategy/new    | StrategyEditor  | Yes        | Create new strategy |
 * | /strategy/123    | StrategyEditor  | Yes        | Edit strategy #123  |
 * | /                | (redirect)      | -          | Goes to /dashboard  |
 *
 * ============================================================
 * URL PARAMETERS
 * ============================================================
 * The path "/strategy/:id" has a parameter called :id
 *
 * Example URLs:
 * - /strategy/1   → id = "1"
 * - /strategy/42  → id = "42"
 * - /strategy/new → This matches the "/strategy/new" route first!
 *
 * In the component, we can access this using:
 *   const { id } = useParams();
 */

// ============================================================
// PROTECTED ROUTE COMPONENT
// ============================================================

/**
 * ProtectedRoute - Guards routes that require authentication
 *
 * What is a protected route?
 * - Some pages should only be visible to logged-in users
 * - If not logged in, redirect to the login page
 * - This prevents unauthorized access to the dashboard
 *
 * How it works:
 * 1. Check if user is authenticated (has valid JWT token)
 * 2. If yes → render the requested page (children)
 * 3. If no → redirect to /login
 *
 * Usage:
 *   <ProtectedRoute>
 *     <Dashboard />    // Only shown if logged in
 *   </ProtectedRoute>
 *
 * Technical details:
 * - {children} is a special React prop that contains nested elements
 * - <Navigate to="/login" /> is a React Router redirect
 * - authService.isAuthenticated() checks localStorage for JWT token
 *
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - The component to render if authenticated
 * @returns {React.ReactNode} Either the children or a redirect
 */
const ProtectedRoute = ({ children }) => {
  // Check if user has a valid token
  const isLoggedIn = authService.isAuthenticated();

  // If logged in, show the page. If not, redirect to login.
  return isLoggedIn ? children : <Navigate to="/login" />;
};

// ============================================================
// MAIN APP COMPONENT
// ============================================================

/**
 * App - The root component of the application
 *
 * This function returns JSX that defines:
 * 1. The Router wrapper (enables routing)
 * 2. All the routes in the application
 *
 * The <Router> component must wrap everything that needs routing.
 * The <Routes> component is the parent of all <Route> elements.
 */
function App() {
  return (
    // BrowserRouter enables client-side routing
    // Uses HTML5 history API (clean URLs like /dashboard)
    <Router>
      <div className="App">

        {/* Routes: Container for all route definitions */}
        {/* React Router matches the current URL to one of these routes */}
        <Routes>

          {/* ==========================================
              PUBLIC ROUTES
              ==========================================
              These pages are accessible without logging in.
              Anyone can visit /login and /signup.
          */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* ==========================================
              PROTECTED ROUTES
              ==========================================
              These pages require authentication.
              If not logged in, user is redirected to /login.

              Note: We wrap each protected component with <ProtectedRoute>
          */}

          {/* Dashboard: Shows all user's strategies */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          {/* Create new strategy */}
          {/* Note: This route must come BEFORE /strategy/:id */}
          {/* Otherwise "new" would be treated as an id! */}
          <Route
            path="/strategy/new"
            element={
              <ProtectedRoute>
                <StrategyEditor />
              </ProtectedRoute>
            }
          />

          {/* Edit existing strategy */}
          {/* :id is a URL parameter - can be any value */}
          {/* In StrategyEditor, we use useParams() to get the id */}
          <Route
            path="/strategy/:id"
            element={
              <ProtectedRoute>
                <StrategyEditor />
              </ProtectedRoute>
            }
          />

          {/* ==========================================
              DEFAULT ROUTE
              ==========================================
              If someone visits the root URL (/), redirect to dashboard.
              The ProtectedRoute there will then check if they're logged in.
          */}
          <Route path="/" element={<Navigate to="/dashboard" />} />

        </Routes>
      </div>
    </Router>
  );
}

export default App;
