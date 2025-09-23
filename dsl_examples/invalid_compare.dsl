strategy "Incomplete Compare" {
    symbols: AAPL
    capital: $10000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    if rsi > 30 and price > {
        buy AAPL
    }
}