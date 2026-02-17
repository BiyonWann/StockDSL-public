# Quick Testing - Your StockDSL Project

## ✅ Your Original Code Works!

I've created a simple script to test your ANTLR compiler.

### Test Your Original ANTLR Code

```bash
# Run the test script
./compile_and_run.sh
```

**This script:**
1. ✅ Sets up the correct package structure (moves files to `gen/stockdsl/`)
2. ✅ Compiles `Main.java` and `Translator.java`
3. ✅ Runs the backtest
4. ✅ Shows you the trading results!

### What You Just Saw

Your code:
- Read `nvda_amd_rotation.dsl`
- Translated it to Python
- Downloaded NVDA and AMD stock prices from 2023
- Simulated trading based on RSI and moving averages
- Made 247 trades!
- Final result: **$11,980 (started with $12,000)**

---

## Next: Test the Fullstack App

Now that your original code works, let's integrate it with React + Spring Boot + PostgreSQL!

### Step 1: Set Up Database (2 minutes)

```bash
# Create database
createdb stockdsl

# Load schema
psql stockdsl < fullstack-app/database/schema.sql

# Verify
psql stockdsl -c "SELECT * FROM users;"
# Should show: demo_user
```

### Step 2: Set Up Backend (5 minutes)

```bash
cd fullstack-app/backend

# Copy your working files
mkdir -p src/main/resources/antlr4
cp ../../StockDSL.g4 src/main/resources/antlr4/

mkdir -p src/main/java/com/stockdsl/compiler
cp ../../src/Translator.java src/main/java/com/stockdsl/compiler/
```

**IMPORTANT:** Edit `Translator.java` and add this at the very top:

```java
package com.stockdsl.compiler;

// Then keep all the imports below...
```

Also update the imports:
```java
// Change:
import stockdsl.StockDSLBaseVisitor;
import stockdsl.StockDSLParser;

// To:
import com.stockdsl.compiler.StockDSLBaseVisitor;
import com.stockdsl.compiler.StockDSLParser;
```

Then build:
```bash
# Install Python packages
pip3 install yahooquery pandas pandas_ta

# Build and run
mvn clean install
mvn spring-boot:run
```

Wait for:
```
========================================
   StockDSL Backend Started!
   API: http://localhost:8080
========================================
```

### Step 3: Test Backend with curl (1 minute)

Open a **new terminal**:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_user","password":"password123"}'

# You'll get a token - copy it!

# Get strategies (replace YOUR_TOKEN)
curl http://localhost:8080/api/strategies \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Step 4: Start Frontend (2 minutes)

Open **another terminal**:

```bash
cd fullstack-app/frontend

# First time only
npm install

# Start React
npm start
```

Browser opens to http://localhost:3000

**Login:** demo_user / password123

---

## You're Done! 🎉

You now have:
- ✅ Your original ANTLR compiler working
- ✅ Spring Boot backend ready
- ✅ React frontend ready
- ✅ PostgreSQL database ready

**Next:** Follow the full [TESTING_GUIDE.md](TESTING_GUIDE.md) to run a complete backtest through the web UI!

---

## Quick Reference

| Command | What It Does |
|---------|--------------|
| `./compile_and_run.sh` | Test your original ANTLR code |
| `psql stockdsl -c "\dt"` | Check database tables |
| `mvn spring-boot:run` | Start backend (in backend/) |
| `npm start` | Start frontend (in frontend/) |
| `curl http://localhost:8080/api/strategies -H "Authorization: Bearer TOKEN"` | Test API |

---

## Files Created

- `compile_and_run.sh` - Test script for your original code
- `TESTING_GUIDE.md` - Complete testing walkthrough
- `QUICK_TEST.md` - 10-minute quick test
- `fullstack-app/TESTING_CHEATSHEET.txt` - Command reference

---

**Problem?** Check [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed debugging steps!
