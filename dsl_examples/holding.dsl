strategy "holding strategy" {
    symbols: AMD
    capital: $12000
    timeframe: daily
    period: "2023-01-01" to "2024-01-01"

    if rsi > 60 and price > sma(AMD, 50) and sma(AMD, 50) > sma(AMD, 200) {
        buy AMD
    }

    if rsi < 50 or price < sma(AMD, 50) {
        sell AMD
    }
}