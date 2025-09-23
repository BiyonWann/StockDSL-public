strategy "Reversal Entry" {
    use "momentum_core"
    symbols: NFLX, DIS
    capital: $18000
    risk_per_trade: 1%
    timeframe: weekly
    period: "2023-01-01" to "2024-01-01"

    if sma(NFLX, 50) < sma(NFLX, 200) and rsi(NFLX) < 30 {
        buy NFLX
    }

    if zscore(price(DIS)) > 2 and rsi(DIS) > 70 {
        sell DIS
    }
}
