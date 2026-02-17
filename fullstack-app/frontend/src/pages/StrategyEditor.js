import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import strategyService from '../services/strategyService';

/**
 * StrategyEditor Component
 *
 * ============================================================
 * WHAT DOES THIS PAGE DO?
 * ============================================================
 * This is where users write and edit their trading strategies
 * using our custom DSL (Domain Specific Language).
 *
 * Two modes:
 * 1. CREATE mode: When URL is /strategy/new (no id)
 * 2. EDIT mode: When URL is /strategy/123 (has id)
 *
 * ============================================================
 * KEY FEATURES
 * ============================================================
 * - Two-column layout: Editor on left, Help panel on right
 * - Collapsible help panel for more coding space
 * - DSL syntax highlighting (placeholder for future)
 * - Real-time validation feedback
 *
 * ============================================================
 * REACT CONCEPTS USED
 * ============================================================
 * - useState: Managing form data and UI state
 * - useEffect: Loading existing strategy on page load
 * - useParams: Getting the strategy ID from URL
 * - useNavigate: Redirecting after save
 * - Conditional rendering: Showing different UI based on state
 */
function StrategyEditor() {
  // ============================================================
  // REACT HOOKS
  // ============================================================

  // Get the 'id' from URL (e.g., /strategy/123 -> id = "123")
  // If URL is /strategy/new, id will be undefined
  const { id } = useParams();

  // useNavigate lets us redirect to other pages programmatically
  const navigate = useNavigate();

  // ============================================================
  // STATE VARIABLES
  // ============================================================

  // Strategy data (what we send to the backend)
  const [strategy, setStrategy] = useState({
    name: '',
    description: '',
    dslCode: ''
  });

  // UI state
  const [loading, setLoading] = useState(false);      // Loading existing strategy?
  const [saving, setSaving] = useState(false);        // Currently saving?
  const [error, setError] = useState('');             // Error message
  const [showHelp, setShowHelp] = useState(true);     // Show help panel?

  // ============================================================
  // LOAD EXISTING STRATEGY (if editing)
  // ============================================================

  /**
   * useEffect runs when component mounts
   *
   * If 'id' exists in URL, we're editing an existing strategy.
   * So we need to load its data from the backend.
   */
  useEffect(() => {
    if (id) {
      loadStrategy();
    }
  }, [id]);

  /**
   * Fetch strategy from backend
   *
   * API call: GET /api/strategies/{id}
   * Response: { id, name, description, dslCode, createdAt, ... }
   */
  const loadStrategy = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await strategyService.getById(id);
      setStrategy({
        name: data.name,
        description: data.description || '',
        dslCode: data.dslCode
      });
    } catch (err) {
      console.error('Failed to load strategy:', err);
      setError('Failed to load strategy. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // ============================================================
  // EVENT HANDLERS
  // ============================================================

  /**
   * Handle input field changes
   *
   * This is a reusable handler for all input fields.
   * It uses the field's "name" attribute to know which
   * property to update in state.
   *
   * Example: <input name="title" /> -> updates strategy.title
   */
  const handleChange = (e) => {
    setStrategy({
      ...strategy,                    // Keep existing values
      [e.target.name]: e.target.value // Update the changed field
    });
  };

  /**
   * Save strategy to backend
   *
   * If id exists: PUT /api/strategies/{id} (update)
   * If no id: POST /api/strategies (create new)
   */
  const handleSave = async () => {
    // Validation
    if (!strategy.name.trim()) {
      setError('Please enter a strategy name');
      return;
    }
    if (!strategy.dslCode.trim()) {
      setError('Please enter DSL code');
      return;
    }

    try {
      setSaving(true);
      setError('');

      if (id) {
        await strategyService.update(id, strategy);
      } else {
        await strategyService.create(strategy);
      }

      // Success - go back to dashboard
      navigate('/dashboard');
    } catch (err) {
      console.error('Failed to save strategy:', err);
      setError(err.response?.data?.message || 'Failed to save strategy. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  /**
   * Insert example code into the editor
   *
   * This helps users get started quickly by providing
   * a working example they can modify.
   */
  const insertExample = (exampleCode) => {
    setStrategy({
      ...strategy,
      dslCode: exampleCode
    });
  };

  // ============================================================
  // LOADING STATE
  // ============================================================

  if (loading) {
    return (
      <div style={styles.loadingContainer}>
        <p>Loading strategy...</p>
      </div>
    );
  }

  // ============================================================
  // MAIN RENDER
  // ============================================================

  return (
    <div style={styles.container}>

      {/* ========================================
          HEADER
          ======================================== */}
      <div style={styles.header}>
        <div style={styles.headerLeft}>
          <button onClick={() => navigate('/dashboard')} style={styles.backButton}>
            ← Back
          </button>
          <h1 style={styles.title}>
            {id ? 'Edit Strategy' : 'Create New Strategy'}
          </h1>
        </div>
        <div style={styles.headerRight}>
          <button
            onClick={() => setShowHelp(!showHelp)}
            style={styles.helpToggle}
          >
            {showHelp ? 'Hide Help' : 'Show Help'}
          </button>
        </div>
      </div>

      {/* ========================================
          MAIN CONTENT (Two-column layout)
          ======================================== */}
      <div style={styles.mainContent}>

        {/* ----------------------------------------
            LEFT COLUMN: Editor
            ---------------------------------------- */}
        <div style={{
          ...styles.editorColumn,
          width: showHelp ? '60%' : '100%'
        }}>

          {/* Strategy Name */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Strategy Name</label>
            <input
              type="text"
              name="name"
              value={strategy.name}
              onChange={handleChange}
              placeholder="e.g., RSI Mean Reversion"
              style={styles.input}
            />
          </div>

          {/* Description */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Description (optional)</label>
            <input
              type="text"
              name="description"
              value={strategy.description}
              onChange={handleChange}
              placeholder="Brief description of your strategy..."
              style={styles.input}
            />
          </div>

          {/* DSL Code Editor */}
          <div style={styles.formGroup}>
            <label style={styles.label}>DSL Code</label>
            <textarea
              name="dslCode"
              value={strategy.dslCode}
              onChange={handleChange}
              placeholder="Write your trading strategy here..."
              style={styles.codeEditor}
              spellCheck={false}
            />
          </div>

          {/* Error Message */}
          {error && (
            <div style={styles.errorBox}>
              {error}
            </div>
          )}

          {/* Action Buttons */}
          <div style={styles.buttonRow}>
            <button
              onClick={handleSave}
              disabled={saving}
              style={{
                ...styles.saveButton,
                opacity: saving ? 0.7 : 1,
                cursor: saving ? 'not-allowed' : 'pointer'
              }}
            >
              {saving ? 'Saving...' : 'Save Strategy'}
            </button>
            <button
              onClick={() => navigate('/dashboard')}
              style={styles.cancelButton}
            >
              Cancel
            </button>
          </div>
        </div>

        {/* ----------------------------------------
            RIGHT COLUMN: Help Panel
            ---------------------------------------- */}
        {showHelp && (
          <div style={styles.helpColumn}>
            <div style={styles.helpPanel}>

              <h2 style={styles.helpTitle}>StockDSL Language Guide</h2>

              {/* -------------------- OVERVIEW -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Overview</h3>
                <p style={styles.helpText}>
                  StockDSL is a simple language for writing trading strategies.
                  Your code is compiled to Python and executed against historical
                  stock data to simulate trades.
                </p>
              </section>

              {/* -------------------- BASIC STRUCTURE -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Basic Structure</h3>
                <pre style={styles.codeBlock}>
{`strategy "My Strategy" {
  // Configuration
  symbols: AAPL, MSFT
  capital: $10000
  period: "2023-01-01" to "2023-12-31"

  // Trading rules
  if rsi < 30 { buy AAPL }
  if rsi > 70 { sell AAPL }
}`}
                </pre>
              </section>

              {/* -------------------- CONFIGURATION -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Configuration Options</h3>
                <table style={styles.helpTable}>
                  <tbody>
                    <tr>
                      <td style={styles.tableCellCode}>symbols: AAPL, MSFT</td>
                      <td style={styles.tableCell}>Stocks to trade</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>capital: $10000</td>
                      <td style={styles.tableCell}>Starting money</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>period: "2023-01-01" to "2023-12-31"</td>
                      <td style={styles.tableCell}>Date range</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>timeframe: daily</td>
                      <td style={styles.tableCell}>daily, weekly, or monthly</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              {/* -------------------- INDICATORS -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Available Indicators</h3>
                <table style={styles.helpTable}>
                  <tbody>
                    <tr>
                      <td style={styles.tableCellCode}>rsi</td>
                      <td style={styles.tableCell}>Relative Strength Index (14-period)</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>sma50</td>
                      <td style={styles.tableCell}>50-day Simple Moving Average</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>sma200</td>
                      <td style={styles.tableCell}>200-day Simple Moving Average</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>zscore</td>
                      <td style={styles.tableCell}>Z-Score (20-period)</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>price</td>
                      <td style={styles.tableCell}>Current stock price</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>volume</td>
                      <td style={styles.tableCell}>Trading volume</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              {/* -------------------- OPERATORS -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Operators</h3>
                <table style={styles.helpTable}>
                  <tbody>
                    <tr>
                      <td style={styles.tableCellCode}>&lt; &gt; &lt;= &gt;= == !=</td>
                      <td style={styles.tableCell}>Comparison</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>and</td>
                      <td style={styles.tableCell}>Both conditions true</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>or</td>
                      <td style={styles.tableCell}>Either condition true</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              {/* -------------------- ACTIONS -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Actions</h3>
                <table style={styles.helpTable}>
                  <tbody>
                    <tr>
                      <td style={styles.tableCellCode}>buy AAPL</td>
                      <td style={styles.tableCell}>Buy shares of AAPL</td>
                    </tr>
                    <tr>
                      <td style={styles.tableCellCode}>sell AAPL</td>
                      <td style={styles.tableCell}>Sell shares of AAPL</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              {/* -------------------- EXAMPLES -------------------- */}
              <section style={styles.helpSection}>
                <h3 style={styles.sectionTitle}>Example Strategies</h3>

                {/* Example 1: RSI */}
                <div style={styles.exampleCard}>
                  <div style={styles.exampleHeader}>
                    <strong>RSI Mean Reversion</strong>
                    <button
                      onClick={() => insertExample(EXAMPLE_RSI)}
                      style={styles.insertButton}
                    >
                      Use This
                    </button>
                  </div>
                  <p style={styles.exampleDesc}>
                    Buy when RSI is oversold (&lt;30), sell when overbought (&gt;70)
                  </p>
                  <pre style={styles.exampleCode}>{EXAMPLE_RSI}</pre>
                </div>

                {/* Example 2: Golden Cross */}
                <div style={styles.exampleCard}>
                  <div style={styles.exampleHeader}>
                    <strong>Golden Cross</strong>
                    <button
                      onClick={() => insertExample(EXAMPLE_GOLDEN_CROSS)}
                      style={styles.insertButton}
                    >
                      Use This
                    </button>
                  </div>
                  <p style={styles.exampleDesc}>
                    Buy when 50-day SMA crosses above 200-day SMA
                  </p>
                  <pre style={styles.exampleCode}>{EXAMPLE_GOLDEN_CROSS}</pre>
                </div>

                {/* Example 3: Multi-condition */}
                <div style={styles.exampleCard}>
                  <div style={styles.exampleHeader}>
                    <strong>Combined Strategy</strong>
                    <button
                      onClick={() => insertExample(EXAMPLE_COMBINED)}
                      style={styles.insertButton}
                    >
                      Use This
                    </button>
                  </div>
                  <p style={styles.exampleDesc}>
                    Use multiple conditions with and/or operators
                  </p>
                  <pre style={styles.exampleCode}>{EXAMPLE_COMBINED}</pre>
                </div>

              </section>

            </div>
          </div>
        )}

      </div>
    </div>
  );
}

// ============================================================
// EXAMPLE STRATEGIES
// ============================================================

const EXAMPLE_RSI = `strategy "RSI Mean Reversion" {
  symbols: AAPL
  capital: $10000
  period: "2023-01-01" to "2023-12-31"

  // Buy when oversold (RSI below 30)
  if rsi < 30 { buy AAPL }

  // Sell when overbought (RSI above 70)
  if rsi > 70 { sell AAPL }
}`;

const EXAMPLE_GOLDEN_CROSS = `strategy "Golden Cross" {
  symbols: MSFT
  capital: $15000
  period: "2023-01-01" to "2023-12-31"

  // Golden cross: short-term MA above long-term
  if sma50 > sma200 { buy MSFT }

  // Death cross: short-term MA below long-term
  if sma50 < sma200 { sell MSFT }
}`;

const EXAMPLE_COMBINED = `strategy "Multi-Signal Strategy" {
  symbols: NVDA, AMD
  capital: $20000
  period: "2023-01-01" to "2023-12-31"

  // Buy NVDA when RSI oversold AND trend is up
  if rsi < 30 and sma50 > sma200 { buy NVDA }

  // Sell NVDA when RSI overbought OR trend reverses
  if rsi > 70 or sma50 < sma200 { sell NVDA }

  // Buy AMD when zscore indicates undervalued
  if zscore < -1 { buy AMD }

  // Sell AMD when zscore indicates overvalued
  if zscore > 1 { sell AMD }
}`;

// ============================================================
// STYLES
// ============================================================

/**
 * Styles Object
 *
 * Using inline styles (JavaScript object) instead of CSS files.
 * This is a common pattern in React for small-medium components.
 *
 * Benefits:
 * - No separate CSS file to manage
 * - Styles are scoped to this component
 * - Easy to understand and modify
 *
 * Tradeoffs:
 * - Can't use CSS features like :hover (need JS workarounds)
 * - Styles aren't cached separately by browser
 * - For large apps, consider CSS-in-JS libraries like styled-components
 */
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
    gap: '20px',
  },
  headerRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  backButton: {
    padding: '8px 16px',
    backgroundColor: '#f0f0f0',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  title: {
    margin: 0,
    fontSize: '24px',
    fontWeight: '600',
    color: '#333',
  },
  helpToggle: {
    padding: '8px 16px',
    backgroundColor: '#e8f4fd',
    color: '#0066cc',
    border: '1px solid #b3d7ff',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
  },

  // Main content
  mainContent: {
    display: 'flex',
    padding: '20px',
    gap: '20px',
    maxWidth: '1600px',
    margin: '0 auto',
  },

  // Editor column
  editorColumn: {
    transition: 'width 0.3s ease',
  },
  formGroup: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    marginBottom: '8px',
    fontWeight: '600',
    fontSize: '14px',
    color: '#333',
  },
  input: {
    width: '100%',
    padding: '12px 15px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    backgroundColor: '#fff',
    boxSizing: 'border-box',
  },
  codeEditor: {
    width: '100%',
    height: '400px',
    padding: '15px',
    fontSize: '14px',
    fontFamily: "'Fira Code', 'Monaco', 'Consolas', monospace",
    border: '1px solid #ddd',
    borderRadius: '8px',
    backgroundColor: '#1e1e1e',
    color: '#d4d4d4',
    boxSizing: 'border-box',
    resize: 'vertical',
    lineHeight: '1.5',
  },
  errorBox: {
    padding: '12px 15px',
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    borderRadius: '8px',
    marginBottom: '20px',
    fontSize: '14px',
  },
  buttonRow: {
    display: 'flex',
    gap: '15px',
  },
  saveButton: {
    padding: '12px 30px',
    backgroundColor: '#22c55e',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  cancelButton: {
    padding: '12px 30px',
    backgroundColor: '#6b7280',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '500',
    cursor: 'pointer',
  },

  // Help column
  helpColumn: {
    width: '40%',
    minWidth: '350px',
  },
  helpPanel: {
    backgroundColor: '#fff',
    borderRadius: '12px',
    padding: '25px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
    maxHeight: 'calc(100vh - 140px)',
    overflowY: 'auto',
  },
  helpTitle: {
    margin: '0 0 20px 0',
    fontSize: '20px',
    fontWeight: '700',
    color: '#1a1a1a',
    borderBottom: '2px solid #e5e7eb',
    paddingBottom: '10px',
  },
  helpSection: {
    marginBottom: '25px',
  },
  sectionTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#374151',
    marginBottom: '10px',
  },
  helpText: {
    fontSize: '14px',
    color: '#6b7280',
    lineHeight: '1.6',
    margin: 0,
  },
  codeBlock: {
    backgroundColor: '#1e1e1e',
    color: '#d4d4d4',
    padding: '15px',
    borderRadius: '8px',
    fontSize: '13px',
    fontFamily: "'Fira Code', 'Monaco', 'Consolas', monospace",
    overflowX: 'auto',
    lineHeight: '1.5',
    margin: 0,
  },
  helpTable: {
    width: '100%',
    fontSize: '13px',
    borderCollapse: 'collapse',
  },
  tableCellCode: {
    padding: '8px 10px',
    fontFamily: "'Fira Code', 'Monaco', 'Consolas', monospace",
    backgroundColor: '#f3f4f6',
    borderRadius: '4px',
    color: '#1f2937',
    whiteSpace: 'nowrap',
  },
  tableCell: {
    padding: '8px 10px',
    color: '#6b7280',
  },

  // Example cards
  exampleCard: {
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    padding: '15px',
    marginBottom: '15px',
    border: '1px solid #e5e7eb',
  },
  exampleHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '8px',
  },
  exampleDesc: {
    fontSize: '13px',
    color: '#6b7280',
    margin: '0 0 10px 0',
  },
  exampleCode: {
    backgroundColor: '#1e1e1e',
    color: '#d4d4d4',
    padding: '12px',
    borderRadius: '6px',
    fontSize: '12px',
    fontFamily: "'Fira Code', 'Monaco', 'Consolas', monospace",
    overflowX: 'auto',
    lineHeight: '1.4',
    margin: 0,
  },
  insertButton: {
    padding: '5px 12px',
    backgroundColor: '#3b82f6',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    fontSize: '12px',
    fontWeight: '500',
    cursor: 'pointer',
  },

  // Loading
  loadingContainer: {
    padding: '40px',
    textAlign: 'center',
  },
};

export default StrategyEditor;
