strategy "RSI Trend With Vol Spike" {
    symbols: NVDA
    capital: $12000
    timeframe: daily
    period: "2023-01-01" to "2024-01-01"

    if (sma(NVDA, 50) > sma(NVDA, 200) and rsi > 55) or (zscore > 2) {
        buy NVDA
    }

    if (rsi > 75) or (sma(NVDA, 50) < sma(NVDA, 200)) {
        sell NVDA
    }
}