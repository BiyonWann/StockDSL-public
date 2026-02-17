-- StockDSL Database Schema
-- PostgreSQL 13+

-- Drop tables if they exist (for development)
DROP TABLE IF EXISTS trades CASCADE;
DROP TABLE IF EXISTS backtests CASCADE;
DROP TABLE IF EXISTS strategies CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- USERS TABLE
-- Stores user accounts for authentication
-- ============================================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt hashed password
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- ============================================================
-- STRATEGIES TABLE
-- Stores user-created trading strategies (DSL code)
-- Each strategy belongs to one user
-- ============================================================
CREATE TABLE strategies (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    dsl_code TEXT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,  -- Future: share strategies
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_strategy_name UNIQUE(user_id, name)
);

-- ============================================================
-- BACKTESTS TABLE
-- Stores results from running backtests
-- Links to both strategy (what was run) and user (who ran it)
-- ============================================================
CREATE TABLE backtests (
    id SERIAL PRIMARY KEY,
    strategy_id INT NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Configuration (from DSL)
    symbols TEXT[],  -- Array of stock symbols
    capital DECIMAL(15, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Results
    final_capital DECIMAL(15, 2) NOT NULL,
    profit_loss DECIMAL(15, 2) NOT NULL,
    profit_loss_percent DECIMAL(5, 2) NOT NULL,
    total_trades INT DEFAULT 0,

    -- Metadata
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INT  -- How long it took to run
);

-- ============================================================
-- TRADES TABLE
-- Stores individual buy/sell actions from backtests
-- Each trade belongs to one backtest
-- ============================================================
CREATE TABLE trades (
    id SERIAL PRIMARY KEY,
    backtest_id INT NOT NULL REFERENCES backtests(id) ON DELETE CASCADE,
    trade_date DATE NOT NULL,
    action VARCHAR(10) NOT NULL CHECK (action IN ('BUY', 'SELL')),
    symbol VARCHAR(10) NOT NULL,
    quantity INT DEFAULT 1 CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    total_value DECIMAL(15, 2) NOT NULL
);

-- ============================================================
-- INDEXES
-- Improve query performance
-- ============================================================

-- Users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- Strategies
CREATE INDEX idx_strategies_user_id ON strategies(user_id);
CREATE INDEX idx_strategies_is_public ON strategies(is_public);
CREATE INDEX idx_strategies_created_at ON strategies(created_at DESC);

-- Backtests
CREATE INDEX idx_backtests_user_id ON backtests(user_id);
CREATE INDEX idx_backtests_strategy_id ON backtests(strategy_id);
CREATE INDEX idx_backtests_executed_at ON backtests(executed_at DESC);

-- Trades
CREATE INDEX idx_trades_backtest_id ON trades(backtest_id);
CREATE INDEX idx_trades_trade_date ON trades(trade_date);
CREATE INDEX idx_trades_symbol ON trades(symbol);

-- ============================================================
-- SAMPLE DATA (For Development/Testing Only)
-- ============================================================

-- Sample user (password: 'password123' - DO NOT USE IN PRODUCTION!)
-- This is a BCrypt hash of 'password123'
INSERT INTO users (username, email, password_hash, first_name, last_name)
VALUES (
    'demo_user',
    'demo@stockdsl.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JEzqLkAZqL5JhqGbWNqG0Y.xqZKu',
    'Demo',
    'User'
);

-- Sample strategy for demo user
INSERT INTO strategies (user_id, name, description, dsl_code, is_public)
VALUES (
    1,
    'RSI Oversold Strategy',
    'Buys when RSI drops below 30, sells when RSI goes above 70',
    'strategy "rsi_oversold" {
    symbols: AAPL, MSFT
    capital: $10000
    period: "2023-01-01" to "2023-12-31"

    if rsi < 30 {
        buy AAPL
        buy MSFT
    }

    if rsi > 70 {
        sell AAPL
        sell MSFT
    }
}',
    TRUE
);

-- Sample strategy 2
INSERT INTO strategies (user_id, name, description, dsl_code, is_public)
VALUES (
    1,
    'Golden Cross Strategy',
    'Buys when 50-day SMA crosses above 200-day SMA',
    'strategy "golden_cross" {
    symbols: SPY
    capital: $15000
    period: "2022-01-01" to "2023-12-31"

    if sma(SPY, 50) > sma(SPY, 200) {
        buy SPY
    }
}',
    FALSE
);

-- ============================================================
-- USEFUL QUERIES (For Reference)
-- ============================================================

-- Get all strategies for a specific user
-- SELECT * FROM strategies WHERE user_id = 1;

-- Get backtest history for a strategy
-- SELECT * FROM backtests WHERE strategy_id = 1 ORDER BY executed_at DESC;

-- Get all trades for a backtest
-- SELECT * FROM trades WHERE backtest_id = 1 ORDER BY trade_date;

-- Get user's total profit/loss across all backtests
-- SELECT SUM(profit_loss) as total_profit FROM backtests WHERE user_id = 1;

-- Get best performing strategy for a user
-- SELECT s.name, b.profit_loss_percent
-- FROM strategies s
-- JOIN backtests b ON s.id = b.strategy_id
-- WHERE s.user_id = 1
-- ORDER BY b.profit_loss_percent DESC
-- LIMIT 1;

-- Get trade statistics for a backtest
-- SELECT
--     COUNT(*) as total_trades,
--     SUM(CASE WHEN action = 'BUY' THEN 1 ELSE 0 END) as buys,
--     SUM(CASE WHEN action = 'SELL' THEN 1 ELSE 0 END) as sells,
--     AVG(price) as avg_price
-- FROM trades
-- WHERE backtest_id = 1;
