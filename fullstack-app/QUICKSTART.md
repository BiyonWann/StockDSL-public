# StockDSL Fullstack - Quick Start Guide

**For Undergraduates Learning Fullstack Development**

This guide walks you through setting up and running your complete fullstack trading platform.

---

## What You're Building

A web application where users can:
1. Write trading strategies in your custom DSL
2. Click a button to run backtests
3. See results instantly (profit/loss, trade history)

**Stack:**
- Frontend: React (runs on port 3000)
- Backend: Spring Boot (runs on port 8080)
- Database: PostgreSQL (runs on port 5432)
- Compiler: Your Java/ANTLR code
- Execution: Python scripts

---

## Prerequisites (Check You Have These)

```bash
# Java 17+
java -version
# Should show: java version "17.x.x" or higher

# Maven
mvn -version
# Should show: Apache Maven 3.x.x

# Node.js and npm
node -version
npm -version
# Should show: v16.x.x or higher

# PostgreSQL
psql --version
# Should show: psql (PostgreSQL) 13.x or higher

# Python 3.8+
python3 --version
pip3 --version
```

If anything is missing, install it first!

---

## Step 1: Database Setup (5 minutes)

### Create Database

```bash
# Option 1: Command line
createdb stockdsl

# Option 2: Using psql
psql postgres
CREATE DATABASE stockdsl;
\q
```

### Run Schema

```bash
cd fullstack-app/database
psql stockdsl < schema.sql
```

### Verify

```bash
psql stockdsl
SELECT * FROM users;
# Should show 1 demo user
\q
```

✅ **Database is ready!**

---

## Step 2: Backend Setup (10 minutes)

### Copy Your ANTLR Files

```bash
cd fullstack-app/backend

# Create ANTLR directory
mkdir -p src/main/resources/antlr4

# Copy grammar file
cp ../../StockDSL.g4 src/main/resources/antlr4/

# Copy your Translator
mkdir -p src/main/java/com/stockdsl/compiler
cp ../../src/Translator.java src/main/java/com/stockdsl/compiler/
```

### Edit Translator.java

Add this line at the very top of `Translator.java`:

```java
package com.stockdsl.compiler;
```

Also update the imports to use the generated package:

```java
// Change this:
// import stockdsl.StockDSLBaseVisitor;
// import stockdsl.StockDSLParser;

// To this:
import com.stockdsl.compiler.StockDSLBaseVisitor;
import com.stockdsl.compiler.StockDSLParser;
```

### Configure Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

### Install Python Dependencies

```bash
pip3 install yahooquery pandas pandas_ta
```

### Build and Run

```bash
# Install dependencies and generate ANTLR files
mvn clean install

# Run the backend
mvn spring-boot:run
```

You should see:
```
========================================
   StockDSL Backend Started!
   API: http://localhost:8080
========================================
```

✅ **Backend is running!**

**Keep this terminal open!**

---

## Step 3: Frontend Setup (5 minutes)

Open a **NEW terminal window**.

```bash
cd fullstack-app/frontend

# Install dependencies
npm install

# Start React
npm start
```

Your browser should automatically open to http://localhost:3000

✅ **Frontend is running!**

**Keep this terminal open too!**

---

## Step 4: Test the Full System

### Test 1: Login

1. Browser opens to http://localhost:3000
2. Click "Login"
3. Use demo account:
   - Username: `demo_user`
   - Password: `password123`
4. Click "Login" button
5. Should redirect to Dashboard

✅ **Authentication works!**

### Test 2: View Strategies

1. On Dashboard, you should see sample strategies
2. Click on "RSI Oversold Strategy"
3. Should show the DSL code

✅ **Database connection works!**

### Test 3: Run a Backtest

1. Click "Run Backtest" button
2. Wait ~30 seconds (downloads stock data)
3. Should see results:
   - Final Capital
   - Profit/Loss
   - Trade History

✅ **Full system works!**

---

## Architecture Diagram (How It All Connects)

```
┌─────────────────────────────────────────────────────────┐
│  BROWSER (http://localhost:3000)                        │
│                                                          │
│  User types:                                            │
│  strategy "my_strategy" {                               │
│      symbols: AAPL                                      │
│      if rsi < 30 { buy AAPL }                           │
│  }                                                       │
│                                                          │
│  [Run Backtest Button]                                  │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ HTTP POST /api/backtests/run/42
                       │ Body: (strategy ID)
                       ↓
┌─────────────────────────────────────────────────────────┐
│  SPRING BOOT BACKEND (localhost:8080)                   │
│                                                          │
│  1. StrategyController receives request                 │
│  2. StrategyService gets DSL code from PostgreSQL       │
│  3. CompilerService compiles:                           │
│     - CharStreams.fromString(dslCode)                   │
│     - StockDSLLexer (ANTLR generated)                   │
│     - StockDSLParser (ANTLR generated)                  │
│     - YOUR Translator.java (translates to Python)       │
│  4. Generates Python code                               │
│  5. ProcessBuilder runs Python                          │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ python3 -c "generated_code"
                       ↓
┌─────────────────────────────────────────────────────────┐
│  PYTHON EXECUTION                                        │
│                                                          │
│  1. import yahooquery                                   │
│  2. Download AAPL stock prices (2023 data)              │
│  3. Calculate RSI indicator                             │
│  4. Simulate trades day-by-day:                         │
│     - Day 1: RSI = 45, do nothing                       │
│     - Day 23: RSI = 28, BUY AAPL @ $150                 │
│     - Day 45: RSI = 72, SELL AAPL @ $165                │
│  5. Print results                                       │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Returns output
                       ↓
┌─────────────────────────────────────────────────────────┐
│  SPRING BOOT (continued)                                 │
│                                                          │
│  6. Parse Python output                                 │
│  7. Save to PostgreSQL:                                 │
│     - backtests table (summary)                         │
│     - trades table (individual trades)                  │
│  8. Return JSON to React                                │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Response: { finalCapital: 11500, ... }
                       ↓
┌─────────────────────────────────────────────────────────┐
│  REACT (displays results)                               │
│                                                          │
│  ╔═══════════════════════════════════════╗            │
│  ║  Backtest Results                     ║            │
│  ║  Starting Capital: $10,000            ║            │
│  ║  Final Capital:    $11,500            ║            │
│  ║  Profit/Loss:      +$1,500 (15%)     ║            │
│  ║                                        ║            │
│  ║  Trade History:                       ║            │
│  ║  Day 23: BOUGHT AAPL @ $150           ║            │
│  ║  Day 45: SOLD AAPL @ $165 (+$15)      ║            │
│  ╚═══════════════════════════════════════╝            │
└─────────────────────────────────────────────────────────┘
```

---

## Explaining This to Others

### For Interviews

> "I built a fullstack trading platform using React, Spring Boot, and PostgreSQL. Users write strategies in a custom DSL I designed using ANTLR. The frontend sends DSL code to my Spring Boot API, which uses my ANTLR compiler to translate it to Python. The Python script downloads real stock data from Yahoo Finance and simulates trading. Results are stored in PostgreSQL and displayed to the user with charts. This demonstrates compiler design, REST API development, database modeling, and fullstack integration."

### For Professors

> "This project combines three major CS concepts: compiler theory (ANTLR parsing), software architecture (MVC pattern with Spring Boot), and database design (normalized schema with foreign keys). The DSL-to-Python translation shows practical application of abstract syntax trees and the visitor pattern. The fullstack architecture demonstrates separation of concerns with clear API boundaries."

### For Technical Deep-Dive

**Question:** "How does the DSL get translated to Python?"

**Answer:** "The flow is: DSL source code → ANTLR Lexer (tokenization) → ANTLR Parser (creates parse tree) → My Translator class (visitor pattern) walks the tree and generates Python strings → ProcessBuilder executes Python. The key insight is using ANTLR's visitor pattern to traverse the abstract syntax tree and emit Python code node by node."

**Question:** "Why not just let users write Python directly?"

**Answer:** "Security and simplicity. Users could execute malicious code if we allowed arbitrary Python. The DSL is sandboxed - they can only express trading rules, not file I/O or system commands. Plus, the DSL is much simpler: 'if rsi < 30 { buy AAPL }' vs complex Python with pandas DataFrames."

**Question:** "How do you handle concurrent backtest requests?"

**Answer:** "Currently sequential - Spring Boot handles one backtest at a time per user. For production, I'd add a job queue (RabbitMQ or Redis) and process backtests asynchronously with WebSocket updates to show progress."

---

## Common Issues

### Backend won't start
```
Error: Could not find or load main class
```
**Fix:** Run `mvn clean install` again

### React shows "Network Error"
**Fix:** Backend isn't running. Start it with `mvn spring-boot:run`

### "Table does not exist"
**Fix:** Run the schema file: `psql stockdsl < database/schema.sql`

### Python imports fail
**Fix:** `pip3 install yahooquery pandas pandas_ta`

### Port 8080 already in use
**Fix:** Kill process: `lsof -ti:8080 | xargs kill -9`

---

## Next Steps

1. ✅ Basic app running
2. Add more pages (Signup, Backtest History)
3. Add CSS styling
4. Add error handling
5. Add loading states
6. Add charts (recharts)
7. Deploy to cloud

---

## Project Checklist

- [ ] PostgreSQL running (port 5432)
- [ ] Database `stockdsl` created
- [ ] Schema loaded (tables exist)
- [ ] Backend running (port 8080)
- [ ] Frontend running (port 3000)
- [ ] Can login as `demo_user`
- [ ] Can view strategies
- [ ] Can run backtest
- [ ] Can see results

---

## File Locations Reference

| What | Where |
|------|-------|
| Database schema | `fullstack-app/database/schema.sql` |
| Backend config | `fullstack-app/backend/src/main/resources/application.properties` |
| Your Translator | `fullstack-app/backend/src/main/java/com/stockdsl/compiler/Translator.java` |
| REST endpoints | `fullstack-app/backend/src/main/java/com/stockdsl/controller/` |
| React pages | `fullstack-app/frontend/src/pages/` |
| API client | `fullstack-app/frontend/src/services/` |

---

## Technologies Summary

| Tech | What It Does | Files |
|------|--------------|-------|
| **PostgreSQL** | Stores users, strategies, results | `database/schema.sql` |
| **Spring Boot** | REST API, calls your compiler | `backend/src/main/java/` |
| **ANTLR** | Parses DSL grammar | `StockDSL.g4`, generated files |
| **Your Translator** | Converts parse tree → Python | `Translator.java` |
| **React** | User interface | `frontend/src/` |
| **Python** | Downloads data, runs backtests | Generated by Translator |

---

**You now have a complete fullstack application!**

Next: Customize it, add features, deploy it, put it on your resume! 🚀
