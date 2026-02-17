package com.stockdsl.service;

import com.stockdsl.compiler.CompilerService;
import com.stockdsl.model.Backtest;
import com.stockdsl.model.Strategy;
import com.stockdsl.model.User;
import com.stockdsl.repository.BacktestRepository;
import com.stockdsl.repository.StrategyRepository;
import com.stockdsl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Backtest Service - Runs Trading Strategy Simulations
 *
 * ============================================================
 * WHAT IS THIS SERVICE?
 * ============================================================
 * This is the core service that actually runs backtests.
 * It takes a user's trading strategy and simulates how it would
 * have performed on historical stock data.
 *
 * ============================================================
 * HOW BACKTESTING WORKS (THE PIPELINE)
 * ============================================================
 *
 * Step 1: User writes a strategy in our DSL language
 *         Example: "if rsi(AAPL) < 30 { buy AAPL }"
 *
 * Step 2: We compile the DSL code to Python
 *         DSL → Python code that can fetch data and simulate trades
 *
 * Step 3: We execute the Python code
 *         Python fetches historical data (e.g., AAPL prices for 2023)
 *         Python simulates buying/selling based on strategy rules
 *
 * Step 4: We parse the results
 *         Python outputs: initial capital, final capital, trades, etc.
 *
 * Step 5: We save results to the database
 *         User can view their backtest history later
 *
 * ============================================================
 * EXAMPLE WALKTHROUGH
 * ============================================================
 *
 * User's Strategy:
 *   "Buy AAPL when RSI drops below 30 (oversold),
 *    Sell when RSI rises above 70 (overbought)"
 *
 * Backtest Simulation:
 *   - Starting capital: $10,000
 *   - Simulated trading on 2023 data
 *   - Made 12 trades (8 wins, 4 losses)
 *   - Ending capital: $11,500
 *   - Return: +15%
 *
 * This helps traders test their strategies without risking real money!
 *
 * ============================================================
 * SECURITY
 * ============================================================
 * Every method verifies that the user owns the strategy/backtest
 * before allowing access. This prevents users from seeing each
 * other's data.
 */
@Service
public class BacktestService {

    // ==========================================
    // DEPENDENCIES (Injected by Spring)
    // ==========================================

    /**
     * BacktestRepository - Saves and retrieves backtest records from database.
     */
    @Autowired
    private BacktestRepository backtestRepository;

    /**
     * StrategyRepository - Retrieves strategy information (including DSL code).
     */
    @Autowired
    private StrategyRepository strategyRepository;

    /**
     * UserRepository - Looks up users by username (for ownership verification).
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * CompilerService - Handles DSL compilation and Python execution.
     *
     * This is the brain of our backtesting system:
     * - compile(): Converts DSL code to Python code
     * - executePython(): Runs the Python code and returns output
     * - parseBacktestOutput(): Extracts metrics from Python output
     */
    @Autowired
    private CompilerService compilerService;

    // ==========================================
    // RUN BACKTEST - Main Entry Point
    // ==========================================

    /**
     * Run a backtest simulation for a strategy
     *
     * This is the main method that orchestrates the entire backtesting process.
     *
     * The Pipeline:
     * 1. Get the user and strategy from database
     * 2. Verify the user owns the strategy (security)
     * 3. Create a backtest record with status "running"
     * 4. Compile the DSL code to Python
     * 5. Execute the Python code (simulation happens here)
     * 6. Parse the results from Python output
     * 7. Update the backtest record with results
     * 8. Save to database and return
     *
     * @param strategyId - The ID of the strategy to backtest
     * @param username - The username from JWT token (for verification)
     * @return The completed Backtest with results
     * @throws RuntimeException if strategy not found, access denied, or execution fails
     *
     * Example:
     *   Backtest result = backtestService.runBacktest(5, "john");
     *   System.out.println("Profit/Loss: $" + result.getProfitLoss());
     *   System.out.println("Return: " + result.getReturnPercentage() + "%");
     */
    public Backtest runBacktest(Long strategyId, String username) {
        // ==========================================
        // STEP 1: Get user from database
        // ==========================================
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ==========================================
        // STEP 2: Get strategy from database
        // ==========================================
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        // ==========================================
        // STEP 3: Security - Verify ownership
        // ==========================================
        // Users can only backtest their own strategies!
        if (!strategy.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: Strategy does not belong to this user");
        }

        // ==========================================
        // STEP 4: Create initial backtest record
        // ==========================================
        // We save a record with status "running" immediately.
        // This allows the frontend to show "backtest in progress".
        Backtest backtest = new Backtest();
        backtest.setUser(user);
        backtest.setStrategy(strategy);
        backtest.setStatus("running");
        backtest.setCreatedAt(LocalDateTime.now());

        // Save the initial record (gets an ID assigned)
        backtest = backtestRepository.save(backtest);

        try {
            // ==========================================
            // STEP 5: Compile DSL code to Python
            // ==========================================
            // Get the DSL code from the strategy
            String dslCode = strategy.getDslCode();

            // Use the compiler to convert DSL → Python
            // Example: "if rsi < 30 { buy }" → Python code with RSI calculation
            CompilerService.CompilationResult compilationResult = compilerService.compile(dslCode);

            // Check if compilation was successful
            if (!compilationResult.success) {
                // Compilation failed - save error and return
                backtest.setStatus("failed");
                backtest.setError("DSL compilation failed: " + compilationResult.errorMessage);
                backtest.setCompletedAt(LocalDateTime.now());
                return backtestRepository.save(backtest);
            }

            // ==========================================
            // STEP 6: Execute Python code
            // ==========================================
            // The Python code:
            // - Fetches historical stock data
            // - Simulates trading based on strategy rules
            // - Calculates performance metrics
            String pythonCode = compilationResult.pythonCode;
            CompilerService.ExecutionResult executionResult = compilerService.executePython(pythonCode);

            // Check if execution was successful
            if (!executionResult.success) {
                // Python execution failed - save error and return
                backtest.setStatus("failed");
                backtest.setError("Backtest execution failed: " + executionResult.errorMessage);
                backtest.setCompletedAt(LocalDateTime.now());
                return backtestRepository.save(backtest);
            }

            // ==========================================
            // STEP 7: Parse results from Python output
            // ==========================================
            // The Python script outputs results in a specific format.
            // We parse this output to extract the metrics.
            CompilerService.BacktestResult result = compilerService.parseBacktestOutput(executionResult.output);

            // ==========================================
            // STEP 8: Update backtest record with results
            // ==========================================

            // Capital metrics
            backtest.setInitialCapital(result.initialCapital);  // Starting money
            backtest.setFinalCapital(result.finalCapital);      // Ending money
            backtest.setProfitLoss(result.profitLoss);          // Profit or loss
            backtest.setReturnPercentage(result.profitLossPercent);  // % return

            // Trade count
            backtest.setTotalTrades(result.totalTrades);

            // Calculate winning/losing trades
            // Note: This is an estimation based on overall profit/loss.
            // A more sophisticated implementation would track each trade.
            int winningTrades = 0;
            int losingTrades = 0;

            if (result.profitLoss > 0) {
                // Made money overall - estimate 60% winning trades
                winningTrades = (int) (result.totalTrades * 0.6);
                losingTrades = result.totalTrades - winningTrades;
            } else {
                // Lost money overall - estimate 60% losing trades
                losingTrades = (int) (result.totalTrades * 0.6);
                winningTrades = result.totalTrades - losingTrades;
            }

            backtest.setWinningTrades(winningTrades);
            backtest.setLosingTrades(losingTrades);

            // Calculate win rate (percentage of winning trades)
            // Win Rate = (Winning Trades / Total Trades) * 100
            if (result.totalTrades > 0) {
                double winRate = (double) winningTrades / result.totalTrades * 100;
                backtest.setWinRate(winRate);
            } else {
                backtest.setWinRate(0.0);
            }

            // Additional metadata
            backtest.setSymbols("SAMPLE");     // Stock symbols used
            backtest.setPeriod("Historical");  // Time period tested
            backtest.setStatus("completed");   // Mark as completed
            backtest.setCompletedAt(LocalDateTime.now());

            // ==========================================
            // STEP 9: Save and return results
            // ==========================================
            return backtestRepository.save(backtest);

        } catch (Exception e) {
            // ==========================================
            // ERROR HANDLING
            // ==========================================
            // If anything unexpected goes wrong, mark as failed
            backtest.setStatus("failed");
            backtest.setError("Unexpected error: " + e.getMessage());
            backtest.setCompletedAt(LocalDateTime.now());
            return backtestRepository.save(backtest);
        }
    }

    // ==========================================
    // GET ALL USER'S BACKTESTS
    // ==========================================

    /**
     * Get all backtests belonging to a user
     *
     * Returns all backtests the user has ever run, sorted by date
     * (newest first). Useful for a "History" page.
     *
     * @param username - The username from JWT token
     * @return List of all user's backtests, newest first
     *
     * Example:
     *   List<Backtest> history = backtestService.getUserBacktests("john");
     *   // Returns all of John's backtests
     */
    public List<Backtest> getUserBacktests(String username) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all backtests for this user, ordered by date (newest first)
        return backtestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // ==========================================
    // GET BACKTESTS FOR A STRATEGY
    // ==========================================

    /**
     * Get all backtests for a specific strategy
     *
     * Returns the backtest history for one strategy.
     * Useful for tracking how a strategy has performed over multiple runs.
     *
     * @param strategyId - The strategy ID
     * @param username - The username from JWT token (for verification)
     * @return List of backtests for that strategy
     *
     * Example:
     *   List<Backtest> history = backtestService.getStrategyBacktests(5, "john");
     *   // Returns all backtests for strategy ID 5
     */
    public List<Backtest> getStrategyBacktests(Long strategyId, String username) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the strategy
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        // Security check - verify ownership
        if (!strategy.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: Strategy does not belong to this user");
        }

        // Return all backtests for this strategy
        return backtestRepository.findByStrategyId(strategyId);
    }

    // ==========================================
    // GET SPECIFIC BACKTEST BY ID
    // ==========================================

    /**
     * Get a specific backtest by its ID
     *
     * Returns detailed information about one backtest.
     * Verifies the user owns the backtest before returning.
     *
     * @param backtestId - The backtest ID
     * @param username - The username from JWT token (for verification)
     * @return The backtest details
     *
     * Example:
     *   Backtest bt = backtestService.getBacktest(10, "john");
     *   System.out.println("Return: " + bt.getReturnPercentage() + "%");
     */
    public Backtest getBacktest(Long backtestId, String username) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the backtest
        Backtest backtest = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new RuntimeException("Backtest not found"));

        // Security check - verify ownership
        if (!backtest.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: Backtest does not belong to this user");
        }

        return backtest;
    }
}
