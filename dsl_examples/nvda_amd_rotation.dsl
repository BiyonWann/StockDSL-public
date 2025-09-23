// alternating between the stronger of NVDA/AMD
strategy "nvda_amd_rotation" {
    symbols: NVDA, AMD
    capital: $12000
    timeframe: daily
    period: "2023-01-01" to "2023-12-31"

    // favor NVDA when stronger
    if (rsi(NVDA) > rsi(AMD) and sma(NVDA, 50) > sma(NVDA, 200)) or (zscore(price(NVDA)) > 1) {
        buy NVDA
        sell AMD
    }

    // favor AMD when strogner
    if (rsi(AMD) > rsi(NVDA) and sma(AMD, 50) > sma(AMD, 200)) or (zscore(price(AMD)) > 1) {
        buy AMD
        sell NVDA
    }
}