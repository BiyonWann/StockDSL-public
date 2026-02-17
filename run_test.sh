#!/bin/bash

# Fix the package structure for testing
echo "Setting up package structure..."

# Create the stockdsl package directory
mkdir -p gen/stockdsl

# Move all .java files to the stockdsl subdirectory (if not already there)
if [ ! -f gen/stockdsl/StockDSLLexer.java ]; then
    echo "Moving generated files to stockdsl package..."
    mv gen/*.java gen/stockdsl/ 2>/dev/null || true
fi

# Also move class files
if [ ! -f gen/stockdsl/StockDSLLexer.class ]; then
    echo "Moving class files to stockdsl package..."
    mv gen/*.class gen/stockdsl/ 2>/dev/null || true
fi

# Update Main.java and Translator.java imports (keep originals)
echo "Compiling with correct package structure..."

# Compile
javac -cp ".:antlr-4.13.2-complete.jar:gen" \
  src/Main.java src/Translator.java

# Run
echo ""
echo "Running Main..."
java -cp ".:antlr-4.13.2-complete.jar:out:gen" Main

