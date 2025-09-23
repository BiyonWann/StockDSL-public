strategy "monthly_trading" {
    symbols: META
    capital: $15000
    timeframe: monthly
    period: "2020-01-01" to "2024-01-01"

    if sma(META, 50) > sma(META, 200) and rsi > 50 {
        buy META
    }

    if sma(META, 50) < sma(META, 200) or rsi < 45 {
        sell META
    }
}
