strategy "Oversold Recovery" {
    use "stat_package"
    symbols: NVDA, AMD
    capital: $12000
    risk_per_trade: 2%
    timeframe: daily
    period: "2022-06-01" to "2023-06-01"

    if rsi(NVDA) < 25 and zscore(price(NVDA)) < -2 {
        buy NVDA
    }

    if rsi(AMD) > 75 or zscore(price(AMD)) > 2 {
        sell AMD
    }
}
