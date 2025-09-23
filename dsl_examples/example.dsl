strategy "Mean Reversion" {
    use "mean_reversion"
    symbols: AAPL, MSFT, TSLA
    capital: $10000
    risk_per_trade: 1%
    timeframe: daily
    period: "2023-01-01" to "2024-01-01"

    if RSI < 30 and SMA(50) > SMA(200) {
        buy AAPL
        buy MSFT
        buy TSLA
    }

    if RSI > 70 or zscore < -2 {
        sell AAPL
        sell MSFT
        sell TSLA
    }
}
