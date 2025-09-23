strategy "Invalid Indicator Strategy" {
    symbols: MSFT

    if macd_cross("MSFT") {
        buy MSFT
    }
}