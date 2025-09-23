// selling on overbought/trend failure.
strategy "no_trade" {
    symbols: AAPL
    capital: $10000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    if (rsi > 55 and price > sma(AAPL, 50)) or (zscore(price) > 1) {
        buy AAPL
    }

    // exiting on weak z-score
    if (rsi > 75) or (price < sma(AAPL, 50) and zscore(price) < -0.5) {
        sell AAPL
    }
}
