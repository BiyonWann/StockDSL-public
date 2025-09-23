// Buy dips only when long-term uptrend holds; sell on snap-back or overbought.
strategy "MSFT Mean Reversion in Uptrend" {
    symbols: MSFT
    capital: $10000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    // entering above 200SMA (long-term trend up)
    if (zscore(price) < -1 and price > sma(MSFT, 200)) {
        buy MSFT
    }

    // exiting when trend fails
    if (zscore(price) > 0.5) or (price < sma(MSFT, 200)) {
        sell MSFT
    }
}