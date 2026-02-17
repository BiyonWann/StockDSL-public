package com.stockdsl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Backtest Entity - Represents a Strategy Simulation Result
 *
 * ============================================================
 * WHAT IS A BACKTEST?
 * ============================================================
 * A backtest simulates running a trading strategy on historical data.
 * Instead of risking real money, we test the strategy on past data
 * to see how it would have performed.
 *
 * Example:
 * - Strategy: "Buy AAPL when RSI < 30, sell when RSI > 70"
 * - Backtest Period: January 2023 - December 2023
 * - Starting Capital: $10,000
 * - Result: Made 12 trades, ended with $11,500 (+15% return)
 *
 * ============================================================
 * BACKTEST METRICS EXPLAINED
 * ============================================================
 *
 * initialCapital: Starting money (e.g., $10,000)
 * finalCapital: Ending money (e.g., $11,500)
 * profitLoss: finalCapital - initialCapital (e.g., $1,500)
 * returnPercentage: (profitLoss / initialCapital) * 100 (e.g., 15%)
 *
 * totalTrades: Number of buy/sell operations (e.g., 12)
 * winningTrades: Trades that made money (e.g., 8)
 * losingTrades: Trades that lost money (e.g., 4)
 * winRate: (winningTrades / totalTrades) * 100 (e.g., 66.67%)
 *
 * ============================================================
 * DATABASE TABLE
 * ============================================================
 * Maps to the 'backtests' table:
 *
 * CREATE TABLE backtests (
 *   id                BIGSERIAL PRIMARY KEY,
 *   user_id           BIGINT NOT NULL REFERENCES users(id),
 *   strategy_id       BIGINT NOT NULL REFERENCES strategies(id),
 *   symbols           VARCHAR(255),
 *   initial_capital   DECIMAL,
 *   final_capital     DECIMAL,
 *   profit_loss       DECIMAL,
 *   return_percentage DECIMAL,
 *   total_trades      INTEGER,
 *   winning_trades    INTEGER,
 *   losing_trades     INTEGER,
 *   win_rate          DECIMAL,
 *   period            VARCHAR(255),
 *   status            VARCHAR(50) NOT NULL,
 *   error             TEXT,
 *   created_at        TIMESTAMP,
 *   completed_at      TIMESTAMP
 * );
 *
 * ============================================================
 * RELATIONSHIPS
 * ============================================================
 * - MANY backtests belong to ONE user (Many-to-One)
 * - MANY backtests belong to ONE strategy (Many-to-One)
 *
 * A user can run many backtests, and a strategy can be
 * backtested multiple times.
 */
@Entity                       // JPA: Maps to database table
@Table(name = "backtests")    // Maps to 'backtests' table
@Data                         // Lombok: generates getters/setters
@NoArgsConstructor            // Lombok: generates Backtest() constructor
@AllArgsConstructor           // Lombok: generates all-args constructor
public class Backtest {

    // ==========================================
    // PRIMARY KEY
    // ==========================================

    /**
     * Unique identifier for this backtest.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // RELATIONSHIPS
    // ==========================================

    /**
     * The user who ran this backtest.
     *
     * @JsonIgnore prevents user details from appearing in API responses.
     * This is important for security and to avoid circular references.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * The strategy that was tested.
     *
     * Note: We DON'T use @JsonIgnore here because we want to show
     * strategy info (name, id) in the backtest results.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;

    // ==========================================
    // BACKTEST CONFIGURATION
    // ==========================================

    /**
     * Stock symbols that were tested.
     *
     * Example: "AAPL,MSFT,GOOGL"
     * Stored as comma-separated string for simplicity.
     */
    @Column
    private String symbols;

    /**
     * Date range tested.
     *
     * Example: "2023-01-01 to 2023-12-31"
     */
    @Column(name = "period")
    private String period;

    // ==========================================
    // CAPITAL METRICS
    // ==========================================

    /**
     * Starting capital for the simulation.
     *
     * Example: 10000.00 (represents $10,000)
     */
    @Column(name = "initial_capital")
    private Double initialCapital;

    /**
     * Ending capital after all trades.
     *
     * Example: 11500.00 (represents $11,500)
     */
    @Column(name = "final_capital")
    private Double finalCapital;

    /**
     * Net profit or loss.
     *
     * Calculated as: finalCapital - initialCapital
     * Positive = profit, Negative = loss
     *
     * Example: 1500.00 (made $1,500 profit)
     */
    @Column(name = "profit_loss")
    private Double profitLoss;

    /**
     * Return percentage.
     *
     * Calculated as: (profitLoss / initialCapital) * 100
     *
     * Example: 15.0 (15% return on investment)
     */
    @Column(name = "return_percentage")
    private Double returnPercentage;

    // ==========================================
    // TRADE STATISTICS
    // ==========================================

    /**
     * Total number of trades executed.
     *
     * A trade is a complete buy → sell cycle.
     */
    @Column(name = "total_trades")
    private Integer totalTrades;

    /**
     * Number of profitable trades.
     *
     * Trades where the sell price > buy price.
     */
    @Column(name = "winning_trades")
    private Integer winningTrades;

    /**
     * Number of losing trades.
     *
     * Trades where the sell price < buy price.
     */
    @Column(name = "losing_trades")
    private Integer losingTrades;

    /**
     * Win rate percentage.
     *
     * Calculated as: (winningTrades / totalTrades) * 100
     *
     * Example: 66.67 means 66.67% of trades were profitable.
     *
     * Note: A high win rate doesn't guarantee profitability!
     * You could win 90% of trades but still lose money if
     * the losing trades are much larger.
     */
    @Column(name = "win_rate")
    private Double winRate;

    // ==========================================
    // STATUS AND ERROR HANDLING
    // ==========================================

    /**
     * Current status of the backtest.
     *
     * Possible values:
     * - "running": Backtest is currently executing
     * - "completed": Backtest finished successfully
     * - "failed": Backtest encountered an error
     */
    @Column(nullable = false)
    private String status;

    /**
     * Error message if the backtest failed.
     *
     * Only populated when status = "failed".
     * Contains details about what went wrong.
     *
     * Examples:
     * - "DSL compilation failed: syntax error on line 5"
     * - "Backtest execution failed: could not fetch data for INVALID"
     */
    @Column(columnDefinition = "TEXT")
    private String error;

    // ==========================================
    // TIMESTAMPS
    // ==========================================

    /**
     * When this backtest was started.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * When this backtest finished (success or failure).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ==========================================
    // LIFECYCLE CALLBACKS
    // ==========================================

    /**
     * Set default values when backtest is first created.
     *
     * @PrePersist runs before the first INSERT.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "running";  // Default status
        }
    }

    // ==========================================
    // MANUAL GETTERS AND SETTERS
    // ==========================================
    // Backup in case Lombok isn't working properly.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }

    public String getSymbols() { return symbols; }
    public void setSymbols(String symbols) { this.symbols = symbols; }

    public Double getInitialCapital() { return initialCapital; }
    public void setInitialCapital(Double initialCapital) { this.initialCapital = initialCapital; }

    public Double getFinalCapital() { return finalCapital; }
    public void setFinalCapital(Double finalCapital) { this.finalCapital = finalCapital; }

    public Double getProfitLoss() { return profitLoss; }
    public void setProfitLoss(Double profitLoss) { this.profitLoss = profitLoss; }

    public Double getReturnPercentage() { return returnPercentage; }
    public void setReturnPercentage(Double returnPercentage) { this.returnPercentage = returnPercentage; }

    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }

    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }

    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }

    public Double getWinRate() { return winRate; }
    public void setWinRate(Double winRate) { this.winRate = winRate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
