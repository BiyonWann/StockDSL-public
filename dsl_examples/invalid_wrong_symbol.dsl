strategy "Invalid Indicator Strategy" {
    if macd_cross("AAPL") {
        buy "AAPL"
    }
}