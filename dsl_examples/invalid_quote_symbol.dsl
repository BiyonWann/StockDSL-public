strategy "SMA Crossover Bad Quotes" {
    symbols: "AAPL", "MSFT"
    capital: $10000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    if sma("AAPL", 50) > sma("AAPL", 200) {
        buy "AAPL"
    }

    if rsi("MSFT") > 70 {
        sell "MSFT"
    }
}