import api from './api';

/**
 * Backtest Service
 *
 * Handles all backtest-related API calls.
 * Uses the api.js instance which automatically includes JWT tokens.
 *
 * What is backtesting?
 * - Testing a trading strategy on historical data
 * - See how the strategy would have performed in the past
 * - Get metrics like profit/loss, win rate, number of trades
 */

const backtestService = {
  /**
   * Run a backtest on a strategy
   *
   * Step by step:
   * 1. User clicks "Run Backtest" button
   * 2. We send POST /api/backtests/run/:strategyId
   * 3. Backend compiles DSL code
   * 4. Backend runs simulation on historical data
   * 5. Backend returns results
   *
   * Example:
   * const result = await backtestService.run(5);
   * console.log(result.profitLoss); // 1500.00
   *
   * @param {number} strategyId - The strategy to test
   * @returns {Promise} Backtest results
   */
  run: async (strategyId) => {
    const response = await api.post(`/backtests/run/${strategyId}`);
    return response.data;
  },

  /**
   * Get all backtests for the current user
   *
   * Returns a list of all backtests, newest first.
   *
   * Example:
   * const backtests = await backtestService.getAll();
   * // [{id: 1, profitLoss: 1500, ...}, {id: 2, profitLoss: -500, ...}]
   *
   * @returns {Promise} List of backtests
   */
  getAll: async () => {
    const response = await api.get('/backtests');
    return response.data;
  },

  /**
   * Get a specific backtest by ID
   *
   * Example:
   * const backtest = await backtestService.getById(5);
   *
   * @param {number} id - The backtest ID
   * @returns {Promise} The backtest
   */
  getById: async (id) => {
    const response = await api.get(`/backtests/${id}`);
    return response.data;
  },

  /**
   * Get all backtests for a specific strategy
   *
   * Shows backtest history for a strategy.
   * Example: "You've run this strategy 3 times"
   *
   * Example:
   * const backtests = await backtestService.getByStrategy(5);
   *
   * @param {number} strategyId - The strategy ID
   * @returns {Promise} List of backtests
   */
  getByStrategy: async (strategyId) => {
    const response = await api.get(`/backtests/strategy/${strategyId}`);
    return response.data;
  },
};

export default backtestService;
