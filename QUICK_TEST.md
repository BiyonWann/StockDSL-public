# Quick Test - See It Work in 10 Minutes!

**Fastest way to see your fullstack app working.**

---

## **What You'll See**

By the end, you'll watch data flow like this:

```
You type DSL in browser
        ↓
React sends HTTP request
        ↓
Spring Boot compiles with YOUR code
        ↓
Python downloads stock prices
        ↓
Results saved to PostgreSQL
        ↓
Charts appear in browser
```

---

## **Step 1: Database (2 minutes)**

```bash
# Create database
createdb stockdsl

# Load tables and sample data
cd fullstack-app/database
psql stockdsl < schema.sql

# Verify demo user exists
psql stockdsl -c "SELECT username FROM users;"
# Should show: demo_user
```

---

## **Step 2: Backend (3 minutes)**

```bash
cd fullstack-app/backend

# Copy your ANTLR files (one-time setup)
mkdir -p src/main/resources/antlr4
cp ../../StockDSL.g4 src/main/resources/antlr4/

mkdir -p src/main/java/com/stockdsl/compiler
cp ../../src/Translator.java src/main/java/com/stockdsl/compiler/

# Add package line to Translator.java
# Open Translator.java and add at the top:
# package com.stockdsl.compiler;

# Install Python packages
pip3 install yahooquery pandas pandas_ta

# Build and run
mvn clean install
mvn spring-boot:run
```

**Look for:**
```
========================================
   StockDSL Backend Started!
   API: http://localhost:8080
========================================
```

**Keep this terminal open!**

---

## **Step 3: Test Backend with curl (2 minutes)**

Open a **new terminal**:

```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_user","password":"password123"}'
```

**You should see:**
```json
{
  "token": "eyJhbGc...",
  "username": "demo_user"
}
```

**Copy the token** and test getting strategies:

```bash
# Replace YOUR_TOKEN with the actual token
curl http://localhost:8080/api/strategies \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**You should see:**
```json
[
  {
    "id": 1,
    "name": "RSI Oversold Strategy",
    "dslCode": "strategy \"rsi_oversold\" { ... }"
  }
]
```

✅ **Backend works!**

---

## **Step 4: Frontend (3 minutes)**

Open a **third terminal**:

```bash
cd fullstack-app/frontend

# Install packages (first time only)
npm install

# Start React
npm start
```

Browser opens to http://localhost:3000

**Login:**
- Username: `demo_user`
- Password: `password123`

Should redirect to Dashboard showing strategies.

✅ **Frontend works!**

---

## **Step 5: See the Flow (Watch This Happen!)**

### Create a Strategy

1. Click "New Strategy"
2. Enter:
   - Name: `Test Flow`
   - Code:
     ```
     strategy "test" {
         symbols: AAPL
         capital: $5000
         period: "2023-01-01" to "2023-03-01"

         if rsi < 30 { buy AAPL }
         if rsi > 70 { sell AAPL }
     }
     ```
3. Click "Save"

**Watch backend terminal:**
```
INFO  Creating new strategy for user: demo_user
INFO  Strategy name: Test Flow
INFO  Saving to database...
INFO  Strategy saved with ID: 2
```

**Check database (new terminal):**
```bash
psql stockdsl -c "SELECT id, name FROM strategies;"
```

**You'll see:**
```
 id |        name
----+---------------------
  1 | RSI Oversold Strategy
  2 | Test Flow            ← Your new strategy!
```

### Run Backtest

1. Click "Run Backtest" on "Test Flow"
2. **Wait 30-60 seconds**

**Watch backend terminal - you'll see EVERYTHING:**

```
INFO  [BacktestController] Received run request for strategy 2
INFO  [BacktestService] Loading strategy from database...
INFO  [BacktestService] Strategy loaded: Test Flow
INFO  [CompilerService] Starting compilation...
INFO  [CompilerService] Creating CharStream from DSL code
INFO  [CompilerService] Lexer: Tokenizing...
INFO  [CompilerService] Parser: Building parse tree...
INFO  [CompilerService] Translator: Visiting tree...
INFO  [CompilerService] Python code generated (234 lines)
INFO  [CompilerService] Executing Python script...

--- Starting Backtest ---
Initial Capital: $5000.00

Downloading AAPL prices from Yahoo Finance...
[############          ] 40%
[####################] 100%

Calculating RSI indicator (14 periods)...
Processing 60 trading days...

Day 15: RSI = 28.5 → BOUGHT 1 AAPL @ $127.50
Day 42: RSI = 72.3 → SOLD 1 AAPL @ $145.00

--- Backtest Finished ---
Trade History:
BOUGHT 1 AAPL @ 127.50
SOLD   1 AAPL @ 145.00

Final Portfolio:
Capital: $5,017.50
Positions: {}

INFO  [CompilerService] Python completed in 48.2 seconds
INFO  [BacktestService] Parsing Python output...
INFO  [BacktestService] Initial capital: 5000.0
INFO  [BacktestService] Final capital: 5017.5
INFO  [BacktestService] Profit: +17.5 (0.35%)
INFO  [BacktestService] Total trades: 2
INFO  [BacktestService] Saving backtest to database...
INFO  [BacktestService] Backtest ID: 3
INFO  [BacktestService] Saving trades...
INFO  [BacktestService] Trade 1 saved
INFO  [BacktestService] Trade 2 saved
INFO  [BacktestService] Returning results to frontend
```

**In React UI:**
```
╔════════════════════════════════════╗
║  Backtest Results ✓                ║
╠════════════════════════════════════╣
║  Strategy: Test Flow               ║
║  Period: 2023-01-01 to 2023-03-01  ║
║                                    ║
║  📊 Performance                    ║
║  Starting Capital: $5,000.00       ║
║  Final Capital:    $5,017.50       ║
║  Profit/Loss:      +$17.50 (0.35%) ║
║  Total Trades:     2               ║
║                                    ║
║  📈 Trade History                  ║
║  ┌──────────┬────────┬───────────┐ ║
║  │ Date     │ Action │ Price     │ ║
║  ├──────────┼────────┼───────────┤ ║
║  │ 02/15/23 │ BUY    │ $127.50   │ ║
║  │ 04/20/23 │ SELL   │ $145.00   │ ║
║  └──────────┴────────┴───────────┘ ║
╚════════════════════════════════════╝
```

**Check database:**
```bash
# See backtest record
psql stockdsl -c "SELECT * FROM backtests WHERE id = 3;"

# See trades
psql stockdsl -c "SELECT * FROM trades WHERE backtest_id = 3;"
```

---

## **Visual Data Flow**

You just watched data move through **5 systems**:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. BROWSER (React)                                          │
│    User types DSL → Clicks "Run Backtest"                   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP POST /api/backtests/run/2
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. SPRING BOOT (Java)                                       │
│    Receives request → Validates JWT → Loads strategy        │
└────────────────────────┬────────────────────────────────────┘
                         │ Passes DSL code to compiler
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. YOUR ANTLR COMPILER (Java)                               │
│    Lexer → Parser → YOUR Translator → Python code           │
└────────────────────────┬────────────────────────────────────┘
                         │ Executes Python script
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. PYTHON (Stock Analysis)                                  │
│    Downloads AAPL prices → Calculates RSI → Simulates trades│
└────────────────────────┬────────────────────────────────────┘
                         │ Returns results to Spring Boot
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. POSTGRESQL (Database)                                    │
│    Spring Boot saves: Backtest summary + Individual trades  │
└────────────────────────┬────────────────────────────────────┘
                         │ Returns saved data
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. BACK TO BROWSER                                          │
│    React displays charts, tables, results                   │
└─────────────────────────────────────────────────────────────┘
```

---

## **What Just Happened?**

1. ✅ You wrote **simple DSL** instead of complex Python
2. ✅ Your **ANTLR compiler** translated it to Python
3. ✅ Python **downloaded real stock data** from Yahoo Finance
4. ✅ Python **simulated trading** based on your rules
5. ✅ Results **saved to database** for future viewing
6. ✅ React **displayed beautiful results**

**All in 10 minutes!**

---

## **Troubleshooting**

| Error | Fix |
|-------|-----|
| "createdb: command not found" | Install PostgreSQL |
| "mvn: command not found" | Install Maven |
| "npm: command not found" | Install Node.js |
| Backend: "Table does not exist" | Run: `psql stockdsl < schema.sql` |
| Backend: "Cannot find Translator" | Add `package com.stockdsl.compiler;` |
| Backend: Port 8080 in use | Kill: `lsof -ti:8080 \| xargs kill -9` |
| Frontend: Network Error | Check backend is running |
| Python: ModuleNotFoundError | Run: `pip3 install yahooquery pandas pandas_ta` |

---

## **You Did It!** 🎉

You now have a **complete fullstack application** that:
- Compiles a custom DSL
- Downloads real stock data
- Runs trading simulations
- Stores everything in a database
- Shows results in a web UI

**This is resume-worthy!**

Next: Customize it, add features, deploy it! 🚀
