package com.stockdsl.repository;

import com.stockdsl.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Strategy database operations
 */
@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    /**
     * Find all strategies belonging to a specific user
     */
    List<Strategy> findByUserId(Long userId);
}
