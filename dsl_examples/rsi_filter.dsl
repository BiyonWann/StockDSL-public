strategy "RSI with Price Filter" {
    symbols: AAPL
    capital: $10000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    // Buy if RSI is low and price is below 150
    if rsi < 30 and close < 150 {
        buy AAPL
    }

    // Sell if RSI is high or price above 170
    if rsi > 70 or close > 170 {
        sell AAPL
    }
}
