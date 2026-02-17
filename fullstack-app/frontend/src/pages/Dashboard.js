import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import strategyService from '../services/strategyService';
import backtestService from '../services/backtestService';

/**
 * Dashboard Page
 *
 * ============================================================
 * WHAT DOES THIS PAGE DO?
 * ============================================================
 * This is the main page users see after logging in.
 * It displays all their trading strategies in a card grid.
 *
 * Features:
 * 1. View all saved strategies
 * 2. Create new strategies (link to StrategyEditor)
 * 3. Edit existing strategies
 * 4. Run backtests on strategies
 * 5. Delete strategies
 *
 * ============================================================
 * DATA FLOW
 * ============================================================
 * 1. Page loads → useEffect triggers
 * 2. loadStrategies() calls backend API
 * 3. Backend checks JWT token, queries database
 * 4. Returns JSON array of user's strategies
 * 5. We store in state → React re-renders
 *
 * ============================================================
 * REACT CONCEPTS USED
 * ============================================================
 * - useState: Managing strategies array, loading state, modals
 * - useEffect: Fetching data when component mounts
 * - Conditional rendering: Loading, empty state, error state
 * - Array.map(): Rendering list of strategy cards
 * - Event handlers: Button clicks for CRUD operations
 */
function Dashboard() {
  // ============================================================
  // STATE VARIABLES
  // ============================================================

  const [strategies, setStrategies] = useState([]);           // Array of strategies from API
  const [loading, setLoading] = useState(true);               // Loading indicator
  const [error, setError] = useState('');                     // Error message
  const [runningBacktest, setRunningBacktest] = useState(null);   // ID of strategy being backtested
  const [deletingStrategy, setDeletingStrategy] = useState(null); // ID of strategy being deleted
  const [backtestResult, setBacktestResult] = useState(null);     // Latest backtest result
  const [showResultsModal, setShowResultsModal] = useState(false);
  const [backtestStrategyName, setBacktestStrategyName] = useState('');

  const navigate = useNavigate();

  // ============================================================
  // LOAD STRATEGIES ON MOUNT
  // ============================================================

  useEffect(() => {
    loadStrategies();
  }, []);

  /**
   * Fetch all strategies from backend
   *
   * API: GET /api/strategies
   * Response: [{ id, name, description, dslCode, createdAt }, ...]
   */
  const loadStrategies = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await strategyService.getAll();
      setStrategies(data);
    } catch (err) {
      console.error('Failed to load strategies:', err);
      setError('Failed to load strategies. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // ============================================================
  // EVENT HANDLERS
  // ============================================================

  /**
   * Run backtest on a strategy
   *
   * API: POST /api/backtests/run/{strategyId}
   * Response: { status, initialCapital, finalCapital, profitLoss, ... }
   */
  const handleRunBacktest = async (strategyId, strategyName) => {
    try {
      setRunningBacktest(strategyId);
      setError('');
      const result = await backtestService.run(strategyId);
      setBacktestResult(result);
      setBacktestStrategyName(strategyName);
      setShowResultsModal(true);
    } catch (err) {
      console.error('Failed to run backtest:', err);
      setBacktestResult({
        status: 'failed',
        error: err.response?.data?.message || 'Failed to run backtest. Check your DSL code.'
      });
      setBacktestStrategyName(strategyName);
      setShowResultsModal(true);
    } finally {
      setRunningBacktest(null);
    }
  };

  /**
   * Delete a strategy
   *
   * API: DELETE /api/strategies/{id}
   */
  const handleDeleteStrategy = async (strategyId, strategyName) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${strategyName}"?\n\nThis action cannot be undone.`
    );

    if (!confirmed) return;

    try {
      setDeletingStrategy(strategyId);
      setError('');
      await strategyService.delete(strategyId);
      // Remove from local state (no need to refetch)
      setStrategies(strategies.filter(s => s.id !== strategyId));
    } catch (err) {
      console.error('Failed to delete strategy:', err);
      setError('Failed to delete strategy. Please try again.');
    } finally {
      setDeletingStrategy(null);
    }
  };

  /**
   * Logout user
   */
  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  /**
   * Close the results modal
   */
  const closeResultsModal = () => {
    setShowResultsModal(false);
  };

  // ============================================================
  // RENDER
  // ============================================================

  return (
    <div style={styles.container}>

      {/* ========================================
          HEADER
          ======================================== */}
      <header style={styles.header}>
        <div style={styles.headerLeft}>
          <h1 style={styles.logo}>StockDSL</h1>
        </div>
        <div style={styles.headerRight}>
          <button onClick={handleLogout} style={styles.logoutButton}>
            Logout
          </button>
        </div>
      </header>

      {/* ========================================
          MAIN CONTENT
          ======================================== */}
      <main style={styles.main}>

        {/* Page Title and Actions */}
        <div style={styles.pageHeader}>
          <div>
            <h2 style={styles.pageTitle}>Your Strategies</h2>
            <p style={styles.pageSubtitle}>
              Create, edit, and backtest your trading strategies
            </p>
          </div>
          <Link to="/strategy/new" style={{ textDecoration: 'none' }}>
            <button style={styles.createButton}>
              + New Strategy
            </button>
          </Link>
        </div>

        {/* Error Message */}
        {error && (
          <div style={styles.errorBanner}>
            {error}
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div style={styles.loadingContainer}>
            <p>Loading strategies...</p>
          </div>
        )}

        {/* Empty State */}
        {!loading && strategies.length === 0 && (
          <div style={styles.emptyState}>
            <div style={styles.emptyIcon}>📈</div>
            <h3 style={styles.emptyTitle}>No strategies yet</h3>
            <p style={styles.emptyText}>
              Create your first trading strategy to get started with backtesting.
            </p>
            <Link to="/strategy/new" style={{ textDecoration: 'none' }}>
              <button style={styles.createButtonLarge}>
                Create Your First Strategy
              </button>
            </Link>
          </div>
        )}

        {/* Strategy Cards Grid */}
        {!loading && strategies.length > 0 && (
          <div style={styles.grid}>
            {strategies.map(strategy => (
              <div key={strategy.id} style={styles.card}>

                {/* Card Header */}
                <div style={styles.cardHeader}>
                  <h3 style={styles.cardTitle}>{strategy.name}</h3>
                  <span style={styles.cardDate}>
                    {new Date(strategy.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {/* Card Description */}
                <p style={styles.cardDescription}>
                  {strategy.description || 'No description'}
                </p>

                {/* Card Actions */}
                <div style={styles.cardActions}>
                  <Link to={`/strategy/${strategy.id}`} style={{ textDecoration: 'none' }}>
                    <button style={styles.editButton}>
                      Edit
                    </button>
                  </Link>

                  <button
                    onClick={() => handleRunBacktest(strategy.id, strategy.name)}
                    disabled={runningBacktest === strategy.id}
                    style={{
                      ...styles.backtestButton,
                      opacity: runningBacktest === strategy.id ? 0.7 : 1,
                      cursor: runningBacktest === strategy.id ? 'not-allowed' : 'pointer'
                    }}
                  >
                    {runningBacktest === strategy.id ? 'Running...' : 'Backtest'}
                  </button>

                  <button
                    onClick={() => handleDeleteStrategy(strategy.id, strategy.name)}
                    disabled={deletingStrategy === strategy.id}
                    style={{
                      ...styles.deleteButton,
                      opacity: deletingStrategy === strategy.id ? 0.7 : 1,
                      cursor: deletingStrategy === strategy.id ? 'not-allowed' : 'pointer'
                    }}
                  >
                    {deletingStrategy === strategy.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>

              </div>
            ))}
          </div>
        )}

      </main>

      {/* ========================================
          BACKTEST RESULTS MODAL
          ======================================== */}
      {showResultsModal && backtestResult && (
        <div onClick={closeResultsModal} style={styles.modalOverlay}>
          <div onClick={(e) => e.stopPropagation()} style={styles.modal}>

            {/* Modal Header */}
            <div style={styles.modalHeader}>
              <h2 style={styles.modalTitle}>Backtest Results</h2>
              <button onClick={closeResultsModal} style={styles.modalClose}>
                ×
              </button>
            </div>

            {/* Strategy Name */}
            <p style={styles.modalStrategy}>
              Strategy: <strong>{backtestStrategyName}</strong>
            </p>

            {/* Success Results */}
            {backtestResult.status === 'completed' && (
              <div style={styles.resultsGrid}>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Initial Capital</span>
                  <span style={styles.resultValue}>
                    ${backtestResult.initialCapital?.toFixed(2)}
                  </span>
                </div>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Final Capital</span>
                  <span style={styles.resultValue}>
                    ${backtestResult.finalCapital?.toFixed(2)}
                  </span>
                </div>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Profit / Loss</span>
                  <span style={{
                    ...styles.resultValue,
                    color: backtestResult.profitLoss >= 0 ? '#22c55e' : '#ef4444'
                  }}>
                    ${backtestResult.profitLoss?.toFixed(2)}
                    <span style={styles.resultPercent}>
                      ({backtestResult.returnPercentage?.toFixed(2)}%)
                    </span>
                  </span>
                </div>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Win Rate</span>
                  <span style={styles.resultValue}>
                    {backtestResult.winRate?.toFixed(2)}%
                  </span>
                </div>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Total Trades</span>
                  <span style={styles.resultValue}>
                    {backtestResult.totalTrades}
                  </span>
                </div>

                <div style={styles.resultCard}>
                  <span style={styles.resultLabel}>Win / Loss</span>
                  <span style={styles.resultValue}>
                    <span style={{ color: '#22c55e' }}>{backtestResult.winningTrades}</span>
                    {' / '}
                    <span style={{ color: '#ef4444' }}>{backtestResult.losingTrades}</span>
                  </span>
                </div>

              </div>
            )}

            {/* Error Results */}
            {backtestResult.status === 'failed' && (
              <div style={styles.errorResult}>
                <strong>Error:</strong> {backtestResult.error}
              </div>
            )}

            {/* Close Button */}
            <button onClick={closeResultsModal} style={styles.modalButton}>
              Close
            </button>

          </div>
        </div>
      )}

    </div>
  );
}

// ============================================================
// STYLES
// ============================================================

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f5f7fa',
  },

  // Header
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '15px 30px',
    backgroundColor: '#ffffff',
    borderBottom: '1px solid #e1e4e8',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
  },
  headerLeft: {
    display: 'flex',
    alignItems: 'center',
  },
  headerRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
  },
  logo: {
    margin: 0,
    fontSize: '24px',
    fontWeight: '700',
    color: '#1a1a1a',
  },
  logoutButton: {
    padding: '8px 20px',
    backgroundColor: '#f0f0f0',
    color: '#333',
    border: '1px solid #ddd',
    borderRadius: '6px',
    fontSize: '14px',
    cursor: 'pointer',
  },

  // Main content
  main: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '30px 20px',
  },
  pageHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
  },
  pageTitle: {
    margin: '0 0 5px 0',
    fontSize: '28px',
    fontWeight: '700',
    color: '#1a1a1a',
  },
  pageSubtitle: {
    margin: 0,
    fontSize: '14px',
    color: '#6b7280',
  },
  createButton: {
    padding: '12px 24px',
    backgroundColor: '#22c55e',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '15px',
    fontWeight: '600',
    cursor: 'pointer',
  },

  // Error banner
  errorBanner: {
    padding: '12px 20px',
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    borderRadius: '8px',
    marginBottom: '20px',
    fontSize: '14px',
  },

  // Loading
  loadingContainer: {
    textAlign: 'center',
    padding: '60px 20px',
    color: '#6b7280',
  },

  // Empty state
  emptyState: {
    textAlign: 'center',
    padding: '80px 20px',
    backgroundColor: '#fff',
    borderRadius: '12px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
  },
  emptyIcon: {
    fontSize: '48px',
    marginBottom: '20px',
  },
  emptyTitle: {
    margin: '0 0 10px 0',
    fontSize: '20px',
    fontWeight: '600',
    color: '#1a1a1a',
  },
  emptyText: {
    margin: '0 0 25px 0',
    fontSize: '14px',
    color: '#6b7280',
  },
  createButtonLarge: {
    padding: '14px 30px',
    backgroundColor: '#3b82f6',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
  },

  // Grid
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
    gap: '20px',
  },

  // Card
  card: {
    backgroundColor: '#fff',
    borderRadius: '12px',
    padding: '24px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
    transition: 'box-shadow 0.2s ease',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '12px',
  },
  cardTitle: {
    margin: 0,
    fontSize: '18px',
    fontWeight: '600',
    color: '#1a1a1a',
  },
  cardDate: {
    fontSize: '12px',
    color: '#9ca3af',
  },
  cardDescription: {
    margin: '0 0 20px 0',
    fontSize: '14px',
    color: '#6b7280',
    lineHeight: '1.5',
  },
  cardActions: {
    display: 'flex',
    gap: '10px',
  },
  editButton: {
    padding: '8px 16px',
    backgroundColor: '#f3f4f6',
    color: '#374151',
    border: '1px solid #e5e7eb',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  backtestButton: {
    padding: '8px 16px',
    backgroundColor: '#3b82f6',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  deleteButton: {
    padding: '8px 16px',
    backgroundColor: '#ef4444',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: '500',
    cursor: 'pointer',
  },

  // Modal
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: '#fff',
    borderRadius: '16px',
    padding: '30px',
    maxWidth: '480px',
    width: '90%',
    maxHeight: '80vh',
    overflow: 'auto',
    boxShadow: '0 20px 50px rgba(0,0,0,0.3)',
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '15px',
  },
  modalTitle: {
    margin: 0,
    fontSize: '22px',
    fontWeight: '700',
    color: '#1a1a1a',
  },
  modalClose: {
    background: 'none',
    border: 'none',
    fontSize: '28px',
    color: '#9ca3af',
    cursor: 'pointer',
    padding: '0 5px',
  },
  modalStrategy: {
    margin: '0 0 25px 0',
    fontSize: '14px',
    color: '#6b7280',
  },
  resultsGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '15px',
    marginBottom: '25px',
  },
  resultCard: {
    backgroundColor: '#f9fafb',
    borderRadius: '10px',
    padding: '16px',
    display: 'flex',
    flexDirection: 'column',
  },
  resultLabel: {
    fontSize: '12px',
    color: '#6b7280',
    marginBottom: '6px',
  },
  resultValue: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#1a1a1a',
  },
  resultPercent: {
    fontSize: '14px',
    fontWeight: '500',
    marginLeft: '5px',
  },
  errorResult: {
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    padding: '20px',
    borderRadius: '10px',
    marginBottom: '25px',
    fontSize: '14px',
  },
  modalButton: {
    width: '100%',
    padding: '14px',
    backgroundColor: '#3b82f6',
    color: '#fff',
    border: 'none',
    borderRadius: '10px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
  },
};

export default Dashboard;
