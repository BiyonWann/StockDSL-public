#!/bin/bash

echo "=== Compiling and Running StockDSL (Original Code) ==="
echo ""

# Make sure gen/stockdsl directory exists
mkdir -p gen/stockdsl

# Check if files are already in stockdsl package
if [ ! -f gen/stockdsl/StockDSLLexer.java ]; then
    echo "Moving ANTLR generated files to correct package structure..."
    mv gen/*.java gen/stockdsl/ 2>/dev/null || true
fi

# Compile Main and Translator
echo "Compiling Main.java and Translator.java..."
javac -cp ".:antlr-4.13.2-complete.jar:gen" \
  -d . \
  src/Main.java src/Translator.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful!"
echo ""

# Run Main
echo "=== Running Main ==="
java -cp ".:antlr-4.13.2-complete.jar:gen" Main

