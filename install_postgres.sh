#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         Installing PostgreSQL for StockDSL                   ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Check if PostgreSQL is already installed
if command -v psql &> /dev/null; then
    echo "✅ PostgreSQL is already installed!"
    psql --version
    echo ""
    echo "Checking if it's running..."
    if brew services list | grep -q "postgresql.*started"; then
        echo "✅ PostgreSQL is running!"
    else
        echo "⚠️  PostgreSQL is installed but not running."
        echo "Starting PostgreSQL..."
        brew services start postgresql@14
    fi
else
    echo "📥 Installing PostgreSQL..."
    brew install postgresql@14

    echo ""
    echo "🚀 Starting PostgreSQL..."
    brew services start postgresql@14

    echo ""
    echo "📝 Adding PostgreSQL to PATH..."
    if ! grep -q "postgresql@14/bin" ~/.zshrc; then
        echo 'export PATH="/opt/homebrew/opt/postgresql@14/bin:$PATH"' >> ~/.zshrc
        echo "Added to ~/.zshrc"
    fi

    echo ""
    echo "⚠️  IMPORTANT: Run this command to update your current terminal:"
    echo "    source ~/.zshrc"
    echo ""
    echo "Or close and reopen your terminal."
fi

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  Next Steps:                                                 ║"
echo "║                                                              ║"
echo "║  1. Close and reopen your terminal (or run: source ~/.zshrc)║"
echo "║  2. Run: createdb stockdsl                                   ║"
echo "║  3. Run: psql stockdsl < fullstack-app/database/schema.sql   ║"
echo "║  4. Run: psql stockdsl -c \"SELECT * FROM users;\"            ║"
echo "╚══════════════════════════════════════════════════════════════╝"
