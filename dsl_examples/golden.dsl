strategy "Golden Cross Strategy" {

    symbols: TSLA
    timeframe: daily

    // Buy when the short-term average is above the long-term average
    if sma(TSLA, 50) > sma(TSLA, 200) {
        buy TSLA
    }

    // Sell when the short-term average is below the long-term average
    if sma(TSLA, 50) < sma(TSLA, 200) {
        sell TSLA
    }
}
