package com.stockdsl.controller;

import com.stockdsl.model.Backtest;
import com.stockdsl.service.BacktestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backtest Controller - REST API for Running and Viewing Backtests
 *
 * ============================================================
 * WHAT IS BACKTESTING?
 * ============================================================
 * Backtesting is testing a trading strategy on historical data.
 * Instead of risking real money, we simulate:
 * - "If I had used this strategy last year, would I have made money?"
 *
 * Example:
 * - Strategy: "Buy AAPL when RSI < 30, sell when RSI > 70"
 * - Backtest: Run this on 2023 data → Made $1,500 profit, 65% win rate
 *
 * This helps traders evaluate strategies before using real money.
 *
 * ============================================================
 * HOW OUR BACKTEST WORKS
 * ============================================================
 * 1. User writes a strategy in our DSL language
 * 2. User clicks "Run Backtest"
 * 3. Backend compiles DSL code to Python
 * 4. Python script fetches historical stock data
 * 5. Python simulates buying/selling based on strategy rules
 * 6. Results are saved to database and returned to user
 *
 * ============================================================
 * REST API ENDPOINTS
 * ============================================================
 *
 * | HTTP Method | URL                              | Description              |
 * |-------------|----------------------------------|--------------------------|
 * | POST        | /api/backtests/run/{strategyId}  | Run a new backtest       |
 * | GET         | /api/backtests                   | Get all user's backtests |
 * | GET         | /api/backtests/{id}              | Get specific backtest    |
 * | GET         | /api/backtests/strategy/{id}     | Get backtests for strategy|
 *
 * ============================================================
 * ANNOTATIONS EXPLAINED
 * ============================================================
 * @RestController = This class handles HTTP requests and returns JSON
 * @RequestMapping = Base URL path for all endpoints in this controller
 *
 * All endpoints require JWT authentication (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/api/backtests")
public class BacktestController {

    // ==========================================
    // DEPENDENCY INJECTION
    // ==========================================

    /**
     * BacktestService handles the business logic for running backtests.
     *
     * @Autowired tells Spring to automatically inject an instance.
     * The service contains the actual backtest execution logic.
     */
    @Autowired
    private BacktestService backtestService;

    // ==========================================
    // RUN BACKTEST
    // ==========================================

    /**
     * Run a backtest on a strategy
     *
     * HTTP Request: POST /api/backtests/run/{strategyId}
     * Example:      POST /api/backtests/run/5
     *
     * This is the main endpoint for executing a backtest.
     *
     * Step by step what happens:
     * 1. Frontend sends POST request with strategy ID
     * 2. We extract the username from the JWT token
     * 3. BacktestService does the heavy lifting:
     *    a. Loads the strategy from database
     *    b. Compiles DSL code to Python
     *    c. Executes Python script with historical data
     *    d. Parses the results
     *    e. Saves backtest record to database
     * 4. We return the results to the frontend
     *
     * @param strategyId - The ID of the strategy to backtest (from URL)
     * @param authentication - User info from JWT token
     * @return Backtest results or error message
     *
     * What is ResponseEntity<?>?
     * - ResponseEntity wraps HTTP response (status code + body)
     * - <?> means the body type can vary (Backtest on success, error map on failure)
     *
     * Example Success Response (200 OK):
     * {
     *   "id": 1,
     *   "initialCapital": 10000.0,
     *   "finalCapital": 11500.0,
     *   "profitLoss": 1500.0,
     *   "returnPercentage": 15.0,
     *   "totalTrades": 12,
     *   "winningTrades": 8,
     *   "losingTrades": 4,
     *   "winRate": 66.67,
     *   "status": "completed"
     * }
     *
     * Example Error Response (400 Bad Request):
     * { "message": "Strategy not found" }
     */
    @PostMapping("/run/{strategyId}")
    public ResponseEntity<?> runBacktest(
            @PathVariable Long strategyId,      // Strategy ID from URL path
            Authentication authentication) {   // User info from JWT token

        try {
            // Get username from JWT token
            String username = authentication.getName();

            // Run the backtest (this does all the work)
            // - Loads strategy, compiles DSL, runs simulation, saves results
            Backtest backtest = backtestService.runBacktest(strategyId, username);

            // Return 200 OK with backtest results
            return ResponseEntity.ok(backtest);

        } catch (Exception e) {
            // If anything goes wrong, return 400 Bad Request with error message
            // Using a Map to create JSON: {"message": "error text"}
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==========================================
    // GET ALL USER'S BACKTESTS
    // ==========================================

    /**
     * Get all backtests for the current user
     *
     * HTTP Request: GET /api/backtests
     *
     * Returns a list of all backtests the user has ever run,
     * sorted by date (newest first).
     *
     * Useful for a "History" page showing past backtest results.
     *
     * @param authentication - User info from JWT token
     * @return List of all user's backtests
     *
     * Example Response:
     * [
     *   {
     *     "id": 2,
     *     "strategy": { "id": 5, "name": "AAPL Strategy" },
     *     "profitLoss": 1500.0,
     *     "returnPercentage": 15.0,
     *     "status": "completed",
     *     "createdAt": "2024-01-15T10:30:00"
     *   },
     *   {
     *     "id": 1,
     *     "strategy": { "id": 3, "name": "NVDA Strategy" },
     *     "profitLoss": -500.0,
     *     "returnPercentage": -5.0,
     *     "status": "completed",
     *     "createdAt": "2024-01-14T09:15:00"
     *   }
     * ]
     */
    @GetMapping
    public ResponseEntity<?> getUserBacktests(Authentication authentication) {
        try {
            // Get username from JWT token
            String username = authentication.getName();

            // Get all backtests for this user
            List<Backtest> backtests = backtestService.getUserBacktests(username);

            // Return 200 OK with the list
            return ResponseEntity.ok(backtests);

        } catch (Exception e) {
            // Return 400 Bad Request with error message
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==========================================
    // GET SPECIFIC BACKTEST BY ID
    // ==========================================

    /**
     * Get a specific backtest by its ID
     *
     * HTTP Request: GET /api/backtests/{id}
     * Example:      GET /api/backtests/5
     *
     * Returns detailed information about one backtest.
     * The service verifies the backtest belongs to the current user.
     *
     * @param id - The backtest ID from the URL path
     * @param authentication - User info from JWT token
     * @return The backtest details
     *
     * What is @PathVariable?
     * - Extracts a value from the URL path
     * - {id} in @GetMapping("/{id}") becomes the 'id' parameter
     * - Example: /api/backtests/5 → id = 5
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBacktest(
            @PathVariable Long id,              // Backtest ID from URL
            Authentication authentication) {   // User info from JWT

        try {
            // Get username from JWT token
            String username = authentication.getName();

            // Get the backtest (service checks ownership)
            Backtest backtest = backtestService.getBacktest(id, username);

            // Return 200 OK with backtest details
            return ResponseEntity.ok(backtest);

        } catch (Exception e) {
            // Return 400 Bad Request with error message
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==========================================
    // GET BACKTESTS FOR A STRATEGY
    // ==========================================

    /**
     * Get all backtests for a specific strategy
     *
     * HTTP Request: GET /api/backtests/strategy/{strategyId}
     * Example:      GET /api/backtests/strategy/3
     *
     * Returns all backtests that were run on one particular strategy.
     *
     * Use case: User wants to see history for a strategy
     * - "How many times have I tested this strategy?"
     * - "Is it performing better or worse over time?"
     *
     * @param strategyId - The strategy ID from the URL path
     * @param authentication - User info from JWT token
     * @return List of backtests for that strategy
     *
     * Example Response:
     * [
     *   { "id": 5, "profitLoss": 1500.0, "createdAt": "2024-01-15" },
     *   { "id": 3, "profitLoss": 800.0, "createdAt": "2024-01-10" },
     *   { "id": 1, "profitLoss": -200.0, "createdAt": "2024-01-05" }
     * ]
     */
    @GetMapping("/strategy/{strategyId}")
    public ResponseEntity<?> getStrategyBacktests(
            @PathVariable Long strategyId,      // Strategy ID from URL
            Authentication authentication) {   // User info from JWT

        try {
            // Get username from JWT token
            String username = authentication.getName();

            // Get all backtests for this strategy
            List<Backtest> backtests = backtestService.getStrategyBacktests(strategyId, username);

            // Return 200 OK with the list
            return ResponseEntity.ok(backtests);

        } catch (Exception e) {
            // Return 400 Bad Request with error message
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==========================================
    // ERROR HANDLING PATTERN
    // ==========================================

    /**
     * Note about error handling in this controller:
     *
     * Each endpoint uses try-catch to handle errors gracefully:
     *
     * try {
     *     // Do the work...
     *     return ResponseEntity.ok(result);      // 200 OK
     * } catch (Exception e) {
     *     Map<String, String> error = new HashMap<>();
     *     error.put("message", e.getMessage());
     *     return ResponseEntity.badRequest().body(error);  // 400 Bad Request
     * }
     *
     * This pattern ensures:
     * 1. Errors don't crash the server
     * 2. Frontend gets a clear error message
     * 3. HTTP status codes correctly indicate success (200) or failure (400)
     *
     * Alternative approaches (not used here but good to know):
     * - @ExceptionHandler: Define error handlers at class or global level
     * - @ControllerAdvice: Global exception handling for all controllers
     */
}
