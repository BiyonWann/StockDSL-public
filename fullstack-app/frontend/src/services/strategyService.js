import api from './api';

/**
 * Strategy Service
 *
 * This service handles all CRUD (Create, Read, Update, Delete) operations
 * for trading strategies.
 *
 * What is a Strategy?
 * - A strategy is a set of trading rules written in our DSL (Domain Specific Language)
 * - Example: "If RSI < 30, buy AAPL. If RSI > 70, sell AAPL."
 * - Strategies are stored in the database and linked to a user
 *
 * CRUD Operations:
 * - Create: POST /api/strategies - Create a new strategy
 * - Read:   GET /api/strategies - Get all strategies for user
 * - Read:   GET /api/strategies/:id - Get one specific strategy
 * - Update: PUT /api/strategies/:id - Update a strategy
 * - Delete: DELETE /api/strategies/:id - Delete a strategy
 *
 * Note: All endpoints require authentication (JWT token).
 * The api.js interceptor automatically adds the token to all requests.
 */

const strategyService = {
  // ==========================================
  // READ - Get All Strategies
  // ==========================================

  /**
   * Get all strategies for the current user
   *
   * HTTP Request: GET /api/strategies
   *
   * What the backend does:
   * 1. Reads JWT token from Authorization header
   * 2. Extracts username from token
   * 3. Queries database: SELECT * FROM strategies WHERE user_id = ?
   * 4. Returns JSON array of strategies
   *
   * @returns {Promise<Array>} Array of strategy objects
   *
   * Example response:
   * [
   *   { id: 1, name: "Mean Reversion", dslCode: "...", createdAt: "2024-01-15" },
   *   { id: 2, name: "Momentum", dslCode: "...", createdAt: "2024-01-16" }
   * ]
   */
  getAll: async () => {
    const response = await api.get('/strategies');
    return response.data;
  },

  // ==========================================
  // READ - Get One Strategy
  // ==========================================

  /**
   * Get a specific strategy by ID
   *
   * HTTP Request: GET /api/strategies/:id
   *
   * What the backend does:
   * 1. Reads JWT token to identify user
   * 2. Queries database for the strategy
   * 3. Verifies the strategy belongs to this user (security!)
   * 4. Returns the strategy if authorized
   *
   * @param {number} id - The strategy ID to fetch
   * @returns {Promise<Object>} The strategy object
   *
   * Example:
   *   const strategy = await strategyService.getById(5);
   *   console.log(strategy.name); // "Mean Reversion"
   */
  getById: async (id) => {
    const response = await api.get(`/strategies/${id}`);
    return response.data;
  },

  // ==========================================
  // CREATE - Add New Strategy
  // ==========================================

  /**
   * Create a new strategy
   *
   * HTTP Request: POST /api/strategies
   *
   * What the backend does:
   * 1. Reads JWT token to identify user
   * 2. Validates the strategy data
   * 3. Inserts new record into database
   * 4. Links strategy to the current user
   * 5. Returns the created strategy (with new ID)
   *
   * @param {Object} strategy - The strategy data
   * @param {string} strategy.name - Name of the strategy
   * @param {string} strategy.dslCode - The DSL code
   * @param {string} strategy.description - Optional description
   * @returns {Promise<Object>} The created strategy (with ID)
   *
   * Example:
   *   const newStrategy = await strategyService.create({
   *     name: "My Strategy",
   *     dslCode: "strategy {...}",
   *     description: "A simple strategy"
   *   });
   *   console.log(newStrategy.id); // 123 (new ID from database)
   */
  create: async (strategy) => {
    const response = await api.post('/strategies', strategy);
    return response.data;
  },

  // ==========================================
  // UPDATE - Modify Existing Strategy
  // ==========================================

  /**
   * Update an existing strategy
   *
   * HTTP Request: PUT /api/strategies/:id
   *
   * What the backend does:
   * 1. Reads JWT token to identify user
   * 2. Finds the strategy by ID
   * 3. Verifies the strategy belongs to this user (security!)
   * 4. Updates the record in database
   * 5. Returns the updated strategy
   *
   * @param {number} id - The strategy ID to update
   * @param {Object} strategy - The new strategy data
   * @returns {Promise<Object>} The updated strategy
   *
   * Example:
   *   const updated = await strategyService.update(5, {
   *     name: "Updated Name",
   *     dslCode: "new code...",
   *     description: "New description"
   *   });
   */
  update: async (id, strategy) => {
    const response = await api.put(`/strategies/${id}`, strategy);
    return response.data;
  },

  // ==========================================
  // DELETE - Remove Strategy
  // ==========================================

  /**
   * Delete a strategy
   *
   * HTTP Request: DELETE /api/strategies/:id
   *
   * What the backend does:
   * 1. Reads JWT token to identify user
   * 2. Finds the strategy by ID
   * 3. Verifies the strategy belongs to this user (security!)
   * 4. Deletes the record from database
   * 5. Returns nothing (204 No Content)
   *
   * Note: This also deletes any backtests associated with this strategy!
   *
   * @param {number} id - The strategy ID to delete
   * @returns {Promise<void>} Nothing (void)
   *
   * Example:
   *   await strategyService.delete(5);
   *   // Strategy with ID 5 is now deleted
   */
  delete: async (id) => {
    await api.delete(`/strategies/${id}`);
  },
};

export default strategyService;
