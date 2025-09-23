import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import stockdsl.StockDSLLexer;
import stockdsl.StockDSLParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String dslFilePath = "dsl_examples/nvda_amd_rotation.dsl";
        String outputPythonFile = "generated_strategy.py";

        System.out.println("--- parsing DSL and generating Python code ---");
        CharStream input = CharStreams.fromFileName(dslFilePath);
        StockDSLLexer lexer = new StockDSLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StockDSLParser parser = new StockDSLParser(tokens);
        ParseTree tree = parser.program();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("parsing failed with syntax errors.");
            return;
        }

        Translator translator = new Translator();
        String pythonCode = translator.visit(tree);

        Path outputPath = Paths.get(outputPythonFile);
        Files.writeString(outputPath, pythonCode);
        System.out.println("successfully generated Python script: " + outputPath.toAbsolutePath());
        System.out.println("--------------------------------------------\n");

        System.out.println("--- executing generated py script ---");
        ProcessBuilder pb = new ProcessBuilder("python", outputPythonFile);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("\n--- python script finished with exit code: " + exitCode + " ---");
    }
}
