package com.stockdsl.compiler;

import com.stockdsl.compiler.StockDSLBaseVisitor;
import com.stockdsl.compiler.StockDSLParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Translator extends StockDSLBaseVisitor<String> {

    private final StringBuilder pythonScript = new StringBuilder();
    private final Map<String, Object> config = new HashMap<>();
    private final List<String> rules = new ArrayList<>();

    private String toPythonClassName(String text) {
        String[] words = text.replace("\"", "").split("\\s+");
        StringBuilder className = new StringBuilder();
        for (String word : words) {
            className.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return className.toString();
    }

    public String visitProgram(StockDSLParser.ProgramContext ctx) {
        for (StockDSLParser.StrategyBlockContext block : ctx.strategyBlock()) {
            visit(block);
        }

        pythonScript.append("import yahooquery as yq\n");
        pythonScript.append("import pandas as pd\n");
        pythonScript.append("import pandas_ta as ta\n\n");

        pythonScript.append("class Portfolio:\n");
        pythonScript.append("    def __init__(self, capital):\n");
        pythonScript.append("        self.capital = capital\n");
        pythonScript.append("        self.positions = {}\n");
        pythonScript.append("        self.history = []\n\n");
        pythonScript.append("    def buy(self, symbol, price, quantity=1):\n");
        pythonScript.append("        cost = price * quantity\n");
        pythonScript.append("        if self.capital >= cost:\n");
        pythonScript.append("            self.capital -= cost\n");
        pythonScript.append("            self.positions[symbol] = self.positions.get(symbol, 0) + quantity\n");
        pythonScript.append("            self.history.append(f\"BOUGHT {quantity} {symbol} @ {price:.2f}\")\n");
        pythonScript.append("        else:\n");
        pythonScript.append("            print(f\"Insufficient capital to buy {symbol}\")\n\n");
        pythonScript.append("    def sell(self, symbol, price, quantity=1):\n");
        pythonScript.append("        if self.positions.get(symbol, 0) >= quantity:\n");
        pythonScript.append("            self.capital += price * quantity\n");
        pythonScript.append("            self.positions[symbol] -= quantity\n");
        pythonScript.append("            self.history.append(f\"SOLD   {quantity} {symbol} @ {price:.2f}\")\n");
        pythonScript.append("            if self.positions[symbol] == 0:\n");
        pythonScript.append("                del self.positions[symbol]\n");
        pythonScript.append("        else:\n");
        pythonScript.append("            print(f\"Not enough shares to sell {symbol}\")\n\n");

        pythonScript.append("class Backtest:\n");
        pythonScript.append("    def __init__(self, symbols, start_date, end_date, capital):\n");
        pythonScript.append("        self.symbols = symbols\n");
        pythonScript.append("        self.start_date = start_date\n");
        pythonScript.append("        self.end_date = end_date\n");
        pythonScript.append("        self.portfolio = Portfolio(capital)\n");
        pythonScript.append("        self.data = self._fetch_data()\n\n");
        pythonScript.append("    def _fetch_data(self):\n");
        pythonScript.append("        data = {}\n");
        pythonScript.append("        try:\n");
        pythonScript.append("            ticker_obj = yq.Ticker(self.symbols)\n");
        pythonScript.append("            hist = ticker_obj.history(start=self.start_date, end=self.end_date)\n");
        pythonScript.append("            if isinstance(hist, pd.DataFrame):\n");
        pythonScript.append("                for symbol in self.symbols:\n");
        pythonScript.append("                    if symbol in hist.index:\n");
        pythonScript.append("                        data[symbol] = hist.loc[symbol]\n");
        pythonScript.append("            else:\n");
        pythonScript.append("                 print(f\"Warning: Unexpected data type returned from API: {type(hist)}\")\n");
        pythonScript.append("        except Exception as e:\n");
        pythonScript.append("            print(f\"Error fetching data with yahooquery: {e}\")\n");
        pythonScript.append("        return data\n\n");
        pythonScript.append("    def run(self, strategy):\n");
        pythonScript.append("        print(\"--- Starting Backtest ---\")\n");
        pythonScript.append("        print(f\"Initial Capital: ${self.portfolio.capital:.2f}\\n\")\n");
        pythonScript.append("        strategy.execute()\n");
        pythonScript.append("        print(\"\\n--- Backtest Finished ---\")\n");
        pythonScript.append("        print(\"Trade History:\")\n");
        pythonScript.append("        if not self.portfolio.history:\n");
        pythonScript.append("            print(\"No trades were executed.\")\n");
        pythonScript.append("        else:\n");
        pythonScript.append("            for trade in self.portfolio.history:\n");
        pythonScript.append("                print(trade)\n");
        pythonScript.append("        print(\"\\nFinal Portfolio:\")\n");
        pythonScript.append("        print(f\"Capital: ${self.portfolio.capital:.2f}\")\n");
        pythonScript.append("        print(\"Positions:\", self.portfolio.positions)\n\n");

        String className = toPythonClassName((String) config.getOrDefault("strategy_name", "MyStrategy"));
        pythonScript.append("class ").append(className).append(":\n");
        pythonScript.append("    def __init__(self, portfolio, data):\n");
        pythonScript.append("        self.portfolio = portfolio\n");
        pythonScript.append("        self.data = data\n\n");
        pythonScript.append("    def crossover(self, series1, series2, i):\n");
        pythonScript.append("        if i == 0:\n");
        pythonScript.append("            return False\n");
        pythonScript.append("        return series1.iloc[i] > series2.iloc[i] and series1.iloc[i-1] <= series2.iloc[i-1]\n\n");
        pythonScript.append("    def execute(self):\n");
        pythonScript.append("        for symbol in self.data:\n");
        pythonScript.append("            df = self.data[symbol]\n");
        pythonScript.append("            if df is None or df.empty:\n");
        pythonScript.append("                print(f\"Skipping {symbol} due to no data.\")\n");
        pythonScript.append("                continue\n");
        pythonScript.append("            df['RSI_14'] = df.ta.rsi(close=df['close'], length=14)\n");
        pythonScript.append("            df['SMA_50'] = df.ta.sma(close=df['close'], length=50)\n");
        pythonScript.append("            df['SMA_200'] = df.ta.sma(close=df['close'], length=200)\n");
        pythonScript.append("            df['ZSCORE_20'] = df.ta.zscore(close=df['close'], length=20)\n\n");
        pythonScript.append("            for i in range(1, len(df)):\n");
        pythonScript.append("                price = df['close'].iloc[i]\n");
        pythonScript.append("                volume = df['volume'].iloc[i]\n");
        pythonScript.append("                rsi = df['RSI_14'].iloc[i]\n");
        pythonScript.append("                sma50 = df['SMA_50'].iloc[i]\n");
        pythonScript.append("                sma200 = df['SMA_200'].iloc[i]\n");
        pythonScript.append("                zscore = df['ZSCORE_20'].iloc[i]\n\n");

        rules.forEach(pythonScript::append);

        pythonScript.append("\n# --- Main Execution Block ---\n");
        @SuppressWarnings("unchecked")
        List<String> symbols = (List<String>) config.getOrDefault("symbols", new ArrayList<>());
        String symbolsStr = symbols.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
        pythonScript.append("symbols = [").append(symbolsStr).append("]\n");
        pythonScript.append("start_date = \"").append(config.getOrDefault("start_date", "2023-01-01")).append("\"\n");
        pythonScript.append("end_date = \"").append(config.getOrDefault("end_date", "2024-01-01")).append("\"\n");
        pythonScript.append("capital = ").append(config.getOrDefault("capital", 10000)).append("\n\n");
        pythonScript.append("backtest = Backtest(symbols, start_date, end_date, capital)\n");
        pythonScript.append("strategy = ").append(className).append("(backtest.portfolio, backtest.data)\n");
        pythonScript.append("backtest.run(strategy)\n");

        return pythonScript.toString();
    }

    public String visitStrategyBlock(StockDSLParser.StrategyBlockContext ctx) {
        config.put("strategy_name", ctx.STRING().getText());
        ctx.statement().forEach(this::visit);
        return null;
    }

    public String visitConfigStmt(StockDSLParser.ConfigStmtContext ctx) {
        if (ctx.symbolList() != null) {
            List<String> symbols = ctx.symbolList().SYMBOL().stream()
                    .map(node -> node.getText())
                    .collect(Collectors.toList());
            config.put("symbols", symbols);
        } else if (ctx.DOLLAR() != null) {
            config.put("capital", Integer.parseInt(ctx.DOLLAR().getText().replace("$", "")));
        } else if (ctx.TIMEFRAME() != null) {
            config.put("timeframe", ctx.TIMEFRAME().getText());
        } else if (ctx.getText().contains("to")) {
            config.put("start_date", ctx.STRING(0).getText().replace("\"", ""));
            config.put("end_date", ctx.STRING(1).getText().replace("\"", ""));
        }
        return null;
    }

    public String visitRule(StockDSLParser.RuleContext ctx) {
        String condition = visit(ctx.expr());
        StringBuilder ruleBuilder = new StringBuilder();
        ruleBuilder.append("                if ").append(condition).append(":\n");
        for (StockDSLParser.ActionContext actionCtx : ctx.action()) {
            ruleBuilder.append(visit(actionCtx));
        }
        rules.add(ruleBuilder.toString());
        return null;
    }

    public String visitAction(StockDSLParser.ActionContext ctx) {
        String actionType = ctx.getChild(0).getText();
        String symbol = ctx.SYMBOL().getText();
        return String.format("                    self.portfolio.%s('%s', price)\n", actionType, symbol);
    }

    public String visitExpr(StockDSLParser.ExprContext ctx) {
        return ctx.logicalTerm().stream().map(this::visit).collect(Collectors.joining(" or "));
    }

    public String visitLogicalTerm(StockDSLParser.LogicalTermContext ctx) {
        return ctx.comparison().stream().map(this::visit).collect(Collectors.joining(" and "));
    }

    public String visitComparison(StockDSLParser.ComparisonContext ctx) {
        String left = visit(ctx.primary(0));
        if (ctx.comparator() != null) {
            String op = ctx.comparator().getText();
            String right = visit(ctx.primary(1));
            return String.format("%s %s %s", left, op, right);
        }
        return left;
    }

    public String visitPrimary(StockDSLParser.PrimaryContext ctx) {
        if (ctx.functionCall() != null) return visit(ctx.functionCall());
        if (ctx.NUMBER() != null) return ctx.NUMBER().getText();
        if (ctx.IDENTIFIER() != null) {
            String id = ctx.IDENTIFIER().getText().toLowerCase();
            switch (id) {
                case "rsi": return "rsi";
                case "price": return "price";
                case "volume": return "volume";
                case "zscore": return "zscore";
                default: return id;
            }
        }
        if (ctx.SYMBOL() != null) return "'" + ctx.SYMBOL().getText() + "'";
        if (ctx.expr() != null) return "(" + visit(ctx.expr()) + ")";
        return "";
    }

    public String visitFunctionCall(StockDSLParser.FunctionCallContext ctx) {
        String funcName = ctx.getChild(0).getText().toLowerCase();
        if ("sma".equals(funcName)) {
            if (ctx.argList() != null && ctx.argList().expr().size() == 2) {
                String length = visit(ctx.argList().expr(1));
                if ("50".equals(length)) return "sma50";
                if ("200".equals(length)) return "sma200";
            }
        }
        if ("crossover".equals(funcName)) {
            String arg1 = visit(ctx.argList().expr(0));
            String arg2 = visit(ctx.argList().expr(1));
            return String.format("self.crossover(df['%S'], df['%S'], i)", arg1.toUpperCase(), arg2.toUpperCase());
        }
        if ("price".equals(funcName) || "volume".equals(funcName) || "rsi".equals(funcName) || "zscore".equals(funcName)) {
            return funcName;
        }
        String args = (ctx.argList() != null) ? visit(ctx.argList()) : "";
        return String.format("%s(%s)", funcName, args);
    }

    public String visitArgList(StockDSLParser.ArgListContext ctx) {
        return ctx.expr().stream().map(this::visit).collect(Collectors.joining(", "));
    }
}