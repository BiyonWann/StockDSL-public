strategy "Crossover Strategy" {

    symbols: TSLA
    timeframe: daily


    // A function call can be a condition by itself
    if crossover(sma(TSLA, 50), sma(TSLA, 200)) {
        buy TSLA
    }

    // Sell when the opposite crossover happens
    if crossover(sma(TSLA, 200), sma(TSLA, 50)) {
        sell TSLA
    }
}