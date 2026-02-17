package com.stockdsl.compiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiler Service - Integrates your ANTLR DSL compiler with Spring Boot
 *
 * This service:
 * 1. Takes DSL code as a string (from HTTP request)
 * 2. Uses ANTLR to parse it (your StockDSLLexer and StockDSLParser)
 * 3. Uses your Translator to convert parse tree → Python code
 * 4. Executes the Python code
 * 5. Returns the results
 *
 * Key Change from Your Original Code:
 * - Original: CharStreams.fromFileName("file.dsl")
 * - New: CharStreams.fromString(dslCode)  ← Takes string from web request
 */
@Service
public class CompilerService {

    @Value("${python.executable:python3}")
    private String pythonExecutable;

    /**
     * Compile DSL code to Python
     *
     * @param dslCode - The DSL source code as a string
     * @return CompilationResult - Contains Python code or errors
     */
    public CompilationResult compile(String dslCode) {
        try {
            // Step 1: Create ANTLR input stream from STRING (not file)
            CharStream input = CharStreams.fromString(dslCode);

            // Step 2: Lexical analysis (tokenize)
            StockDSLLexer lexer = new StockDSLLexer(input);

            // Step 3: Create token stream
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Step 4: Parse (build parse tree)
            StockDSLParser parser = new StockDSLParser(tokens);
            ParseTree tree = parser.program();

            // Step 5: Check for syntax errors
            if (parser.getNumberOfSyntaxErrors() > 0) {
                return CompilationResult.error("Syntax errors found in DSL code");
            }

            // Step 6: Translate parse tree to Python using YOUR Translator
            Translator translator = new Translator();
            String pythonCode = translator.visit(tree);

            // Step 7: Return success
            return CompilationResult.success(pythonCode);

        } catch (Exception e) {
            return CompilationResult.error("Compilation failed: " + e.getMessage());
        }
    }

    /**
     * Execute Python code and capture output
     *
     * @param pythonCode - The generated Python code
     * @return ExecutionResult - Contains backtest results
     */
    public ExecutionResult executePython(String pythonCode) {
        try {
            long startTime = System.currentTimeMillis();

            // Build Python process
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-c", pythonCode);
            pb.redirectErrorStream(true);

            // Start process
            Process process = pb.start();

            // Capture output
            List<String> outputLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                }
            }

            // Wait for completion
            int exitCode = process.waitFor();
            long executionTime = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                return ExecutionResult.success(outputLines, executionTime);
            } else {
                return ExecutionResult.error("Python execution failed with exit code: " + exitCode, outputLines);
            }

        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute Python: " + e.getMessage(), null);
        }
    }

    /**
     * Parse Python output to extract backtest results
     *
     * Python output format:
     * --- Starting Backtest ---
     * Initial Capital: $10000.00
     * BOUGHT 1 NVDA @ 245.50
     * SOLD 1 NVDA @ 275.00
     * --- Backtest Finished ---
     * Final Capital: $11500.00
     * Positions: {'NVDA': 1}
     */
    public BacktestResult parseBacktestOutput(List<String> outputLines) {
        BacktestResult result = new BacktestResult();
        result.trades = new ArrayList<>();

        for (String line : outputLines) {
            // Parse final capital
            if (line.contains("Final Capital:")) {
                String capitalStr = line.replaceAll("[^0-9.]", "");
                result.finalCapital = Double.parseDouble(capitalStr);
            }

            // Parse initial capital
            if (line.contains("Initial Capital:")) {
                String capitalStr = line.replaceAll("[^0-9.]", "");
                result.initialCapital = Double.parseDouble(capitalStr);
            }

            // Parse trades
            if (line.contains("BOUGHT") || line.contains("SOLD")) {
                result.trades.add(line.trim());
            }
        }

        // Calculate profit/loss
        if (result.initialCapital > 0 && result.finalCapital > 0) {
            result.profitLoss = result.finalCapital - result.initialCapital;
            result.profitLossPercent = (result.profitLoss / result.initialCapital) * 100;
        }

        result.totalTrades = result.trades.size();
        result.rawOutput = String.join("\n", outputLines);

        return result;
    }

    // Result classes

    public static class CompilationResult {
        public boolean success;
        public String pythonCode;
        public String errorMessage;

        public static CompilationResult success(String pythonCode) {
            CompilationResult result = new CompilationResult();
            result.success = true;
            result.pythonCode = pythonCode;
            return result;
        }

        public static CompilationResult error(String errorMessage) {
            CompilationResult result = new CompilationResult();
            result.success = false;
            result.errorMessage = errorMessage;
            return result;
        }
    }

    public static class ExecutionResult {
        public boolean success;
        public List<String> output;
        public long executionTimeMs;
        public String errorMessage;

        public static ExecutionResult success(List<String> output, long executionTimeMs) {
            ExecutionResult result = new ExecutionResult();
            result.success = true;
            result.output = output;
            result.executionTimeMs = executionTimeMs;
            return result;
        }

        public static ExecutionResult error(String errorMessage, List<String> output) {
            ExecutionResult result = new ExecutionResult();
            result.success = false;
            result.errorMessage = errorMessage;
            result.output = output;
            return result;
        }
    }

    public static class BacktestResult {
        public double initialCapital;
        public double finalCapital;
        public double profitLoss;
        public double profitLossPercent;
        public int totalTrades;
        public List<String> trades;
        public String rawOutput;
    }
}
