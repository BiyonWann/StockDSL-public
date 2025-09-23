strategy "Breakout Strategy" {
    // --- Configuration ---
    symbols: AAPL, GOOG
    timeframe: daily

    // --- Trading Rules ---
    if price(AAPL) > 150 and volume(AAPL) > 5000000 {
        buy AAPL
    }

    if rsi(AAPL) < 30 {
        buy AAPL
    }
}