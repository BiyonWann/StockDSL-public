import yahooquery as yq
import pandas as pd
import pandas_ta as ta

class Portfolio:
    def __init__(self, capital):
        self.capital = capital
        self.positions = {}
        self.history = []

    def buy(self, symbol, price, quantity=1):
        cost = price * quantity
        if self.capital >= cost:
            self.capital -= cost
            self.positions[symbol] = self.positions.get(symbol, 0) + quantity
            self.history.append(f"BOUGHT {quantity} {symbol} @ {price:.2f}")
        else:
            print(f"Insufficient capital to buy {symbol}")

    def sell(self, symbol, price, quantity=1):
        if self.positions.get(symbol, 0) >= quantity:
            self.capital += price * quantity
            self.positions[symbol] -= quantity
            self.history.append(f"SOLD   {quantity} {symbol} @ {price:.2f}")
            if self.positions[symbol] == 0:
                del self.positions[symbol]
        else:
            print(f"Not enough shares to sell {symbol}")

class Backtest:
    def __init__(self, symbols, start_date, end_date, capital):
        self.symbols = symbols
        self.start_date = start_date
        self.end_date = end_date
        self.portfolio = Portfolio(capital)
        self.data = self._fetch_data()

    def _fetch_data(self):
        data = {}
        try:
            ticker_obj = yq.Ticker(self.symbols)
            hist = ticker_obj.history(start=self.start_date, end=self.end_date)
            if isinstance(hist, pd.DataFrame):
                for symbol in self.symbols:
                    if symbol in hist.index:
                        data[symbol] = hist.loc[symbol]
            else:
                 print(f"Warning: Unexpected data type returned from API: {type(hist)}")
        except Exception as e:
            print(f"Error fetching data with yahooquery: {e}")
        return data

    def run(self, strategy):
        print("--- Starting Backtest ---")
        print(f"Initial Capital: ${self.portfolio.capital:.2f}\n")
        strategy.execute()
        print("\n--- Backtest Finished ---")
        print("Trade History:")
        if not self.portfolio.history:
            print("No trades were executed.")
        else:
            for trade in self.portfolio.history:
                print(trade)
        print("\nFinal Portfolio:")
        print(f"Capital: ${self.portfolio.capital:.2f}")
        print("Positions:", self.portfolio.positions)

class Nvda_amd_rotation:
    def __init__(self, portfolio, data):
        self.portfolio = portfolio
        self.data = data

    def crossover(self, series1, series2, i):
        if i == 0:
            return False
        return series1.iloc[i] > series2.iloc[i] and series1.iloc[i-1] <= series2.iloc[i-1]

    def execute(self):
        for symbol in self.data:
            df = self.data[symbol]
            if df is None or df.empty:
                print(f"Skipping {symbol} due to no data.")
                continue
            df['RSI_14'] = df.ta.rsi(close=df['close'], length=14)
            df['SMA_50'] = df.ta.sma(close=df['close'], length=50)
            df['SMA_200'] = df.ta.sma(close=df['close'], length=200)
            df['ZSCORE_20'] = df.ta.zscore(close=df['close'], length=20)

            for i in range(1, len(df)):
                price = df['close'].iloc[i]
                volume = df['volume'].iloc[i]
                rsi = df['RSI_14'].iloc[i]
                sma50 = df['SMA_50'].iloc[i]
                sma200 = df['SMA_200'].iloc[i]
                zscore = df['ZSCORE_20'].iloc[i]

                if (rsi > rsi and sma50 > sma200) or (zscore > 1):
                    self.portfolio.buy('NVDA', price)
                    self.portfolio.sell('AMD', price)
                if (rsi > rsi and sma50 > sma200) or (zscore > 1):
                    self.portfolio.buy('AMD', price)
                    self.portfolio.sell('NVDA', price)

# --- Main Execution Block ---
symbols = ["NVDA", "AMD"]
start_date = "2023-01-01"
end_date = "2023-12-31"
capital = 12000

backtest = Backtest(symbols, start_date, end_date, capital)
strategy = Nvda_amd_rotation(backtest.portfolio, backtest.data)
backtest.run(strategy)
