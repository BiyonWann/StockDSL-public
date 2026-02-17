package com.stockdsl.repository;

import com.stockdsl.model.Backtest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Backtest database operations
 *
 * Spring Data JPA automatically implements these methods.
 * No need to write SQL queries!
 */
@Repository
public interface BacktestRepository extends JpaRepository<Backtest, Long> {

    /**
     * Find all backtests for a specific user
     *
     * Example: findByUserId(1)
     * SQL: SELECT * FROM backtests WHERE user_id = 1
     *
     * @param userId The user's ID
     * @return List of backtests
     */
    List<Backtest> findByUserId(Long userId);

    /**
     * Find all backtests for a specific strategy
     *
     * Example: findByStrategyId(5)
     * SQL: SELECT * FROM backtests WHERE strategy_id = 5
     *
     * @param strategyId The strategy's ID
     * @return List of backtests
     */
    List<Backtest> findByStrategyId(Long strategyId);

    /**
     * Find all backtests for a user, ordered by creation date (newest first)
     *
     * Example: findByUserIdOrderByCreatedAtDesc(1)
     * SQL: SELECT * FROM backtests WHERE user_id = 1 ORDER BY created_at DESC
     *
     * @param userId The user's ID
     * @return List of backtests, newest first
     */
    List<Backtest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
