# StockDSL Testing Guide - See It All Work!

This guide shows you how to test your fullstack app step-by-step so you can **see the data flowing**.

---

## **Phase 1: Test Your Original ANTLR Code (5 minutes)**

Let's verify your existing compiler works before integrating with Spring Boot.

### Test 1: Compile a Simple Strategy

```bash
cd /Users/biyonwanni/StockDSL-public-snapshot

# Compile and run your existing code
javac -cp ".:antlr-4.13.2-complete.jar:gen" src/Main.java src/Translator.java

# Run it
java -cp ".:antlr-4.13.2-complete.jar:out:gen" Main
```

**What you should see:**
```
--- parsing DSL and generating Python code ---
successfully generated Python script: /Users/.../generated_strategy.py
--------------------------------------------

--- executing generated py script ---
--- Starting Backtest ---
Initial Capital: $12000.00

BOUGHT 1 NVDA @ 245.50
SOLD   1 AMD @ 92.30
...

--- Backtest Finished ---
Final Capital: $11500.00
```

✅ **If you see this, your ANTLR compiler works!**

---

## **Phase 2: Test Database Setup (5 minutes)**

### Test 1: Check PostgreSQL is Running

```bash
# Check if PostgreSQL is running
psql --version

# Should show: psql (PostgreSQL) 13.x or higher
```

If PostgreSQL isn't installed:
- **macOS:** `brew install postgresql && brew services start postgresql`
- **Linux:** `sudo apt-get install postgresql && sudo systemctl start postgresql`

### Test 2: Create Database

```bash
# Create the database
createdb stockdsl

# Verify it was created
psql -l | grep stockdsl
# Should show: stockdsl | your_username | ...
```

### Test 3: Load Schema

```bash
cd fullstack-app/database

# Load the schema (creates tables)
psql stockdsl < schema.sql

# Verify tables were created
psql stockdsl -c "\dt"
```

**What you should see:**
```
         List of relations
 Schema |    Name    | Type  |   Owner
--------+------------+-------+-----------
 public | backtests  | table | your_user
 public | strategies | table | your_user
 public | trades     | table | your_user
 public | users      | table | your_user
```

### Test 4: Check Sample Data

```bash
# Check if demo user exists
psql stockdsl -c "SELECT username, email FROM users;"
```

**Should show:**
```
  username  |       email
------------+--------------------
 demo_user  | demo@stockdsl.com
```

✅ **If you see this, database is ready!**

---

## **Phase 3: Test Backend Alone (10 minutes)**

Now let's test the Spring Boot backend **without** the React frontend.

### Step 1: Copy Your ANTLR Files

```bash
cd fullstack-app/backend

# Create ANTLR directory
mkdir -p src/main/resources/antlr4

# Copy grammar file
cp ../../StockDSL.g4 src/main/resources/antlr4/

# Copy Translator
mkdir -p src/main/java/com/stockdsl/compiler
cp ../../src/Translator.java src/main/java/com/stockdsl/compiler/
```

### Step 2: Add Package Statement to Translator

Edit `src/main/java/com/stockdsl/compiler/Translator.java` and add this line at the **very top**:

```java
package com.stockdsl.compiler;

import org.antlr.v4.runtime.*;
// ... rest of imports
```

Also update the import statements in Translator.java:

**Change from:**
```java
import stockdsl.StockDSLBaseVisitor;
import stockdsl.StockDSLParser;
```

**To:**
```java
import com.stockdsl.compiler.StockDSLBaseVisitor;
import com.stockdsl.compiler.StockDSLParser;
```

### Step 3: Configure Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdsl
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

Replace `YOUR_POSTGRES_USERNAME` with your actual username (often just `postgres`).

### Step 4: Install Python Dependencies

```bash
pip3 install yahooquery pandas pandas_ta
```

### Step 5: Build and Run Backend

```bash
# Install dependencies and generate ANTLR code
mvn clean install

# Run the server
mvn spring-boot:run
```

**What you should see:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2024-01-20 INFO  Starting StockDslApplication...
2024-01-20 INFO  Started StockDslApplication in 3.456 seconds

========================================
   StockDSL Backend Started!
   API: http://localhost:8080
========================================
```

✅ **Backend is running!** Keep this terminal open.

### Step 6: Test API with curl

Open a **NEW terminal** and test the endpoints:

#### Test 1: Health Check
```bash
curl http://localhost:8080/api/strategies
```

**Expected:** Error (401 Unauthorized) because you're not logged in. This is good!

#### Test 2: Login with Demo User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo_user",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "demo_user"
}
```

**COPY THE TOKEN!** You'll need it for next requests.

#### Test 3: Get Strategies (with authentication)
```bash
# Replace YOUR_TOKEN with the token from login
curl http://localhost:8080/api/strategies \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "RSI Oversold Strategy",
    "dslCode": "strategy \"rsi_oversold\" { ... }",
    "createdAt": "2024-01-20T10:00:00"
  }
]
```

✅ **If you see this, backend is working perfectly!**

---

## **Phase 4: Test Frontend Alone (5 minutes)**

Now test React **without** running backtests yet.

### Step 1: Install Dependencies

```bash
cd fullstack-app/frontend

# Install npm packages
npm install
```

### Step 2: Start React

```bash
npm start
```

**What you should see:**
```
Compiled successfully!

You can now view stockdsl-frontend in the browser.

  Local:            http://localhost:3000
  On Your Network:  http://192.168.1.x:3000
```

Your browser should automatically open to http://localhost:3000

### Step 3: Test Login

1. You should see a login page
2. Enter:
   - Username: `demo_user`
   - Password: `password123`
3. Click "Login"

**What should happen:**
- Redirects to Dashboard
- Shows "My Strategies" with sample strategies

### Step 4: Check Browser Console

Press `F12` (or `Cmd+Option+I` on Mac) to open Developer Tools.

Go to **Network** tab and refresh the page.

**You should see:**
```
GET /api/strategies    Status: 200 OK
```

Click on it to see the JSON response.

✅ **If login works and you see strategies, frontend is working!**

---

## **Phase 5: Test Full Flow (The Exciting Part!)**

Now let's test the **complete flow** from DSL code to backtest results.

### Prerequisites

Make sure you have:
1. ✅ Backend running (Terminal 1: `mvn spring-boot:run`)
2. ✅ Frontend running (Terminal 2: `npm start`)
3. ✅ Logged in as demo_user

### Test 1: View Existing Strategy

1. On Dashboard, click "RSI Oversold Strategy"
2. Should show the DSL code
3. **Check browser console** - you'll see:
   ```
   GET /api/strategies/1
   Status: 200 OK
   ```

### Test 2: Create a New Strategy

1. Click "New Strategy" button
2. Enter:
   - **Name:** "Test AAPL"
   - **DSL Code:**
     ```
     strategy "test_aapl" {
         symbols: AAPL
         capital: $10000
         period: "2023-01-01" to "2023-06-01"

         if rsi < 30 {
             buy AAPL
         }

         if rsi > 70 {
             sell AAPL
         }
     }
     ```
3. Click "Save"

**In Backend Terminal, you should see:**
```
INFO  Saving new strategy: Test AAPL
INFO  Strategy saved with ID: 2
```

**In Browser Console:**
```
POST /api/strategies
Status: 200 OK
Response: { "id": 2, "name": "Test AAPL", ... }
```

### Test 3: Run a Backtest (THE BIG ONE!)

1. Click "Run Backtest" on your new strategy
2. **Wait ~30-60 seconds** (downloading stock data)

**Watch the Backend Terminal - You'll see EVERYTHING happening:**

```
INFO  Compiling DSL for strategy: Test AAPL
INFO  Lexer created, tokenizing...
INFO  Parser created, building tree...
INFO  Translator visiting tree...
INFO  Python code generated (245 lines)
INFO  Executing Python...
--- Starting Backtest ---
Initial Capital: $10000.00
Downloading AAPL data from 2023-01-01 to 2023-06-01...
Calculating RSI indicator...
BOUGHT 1 AAPL @ 125.50
SOLD   1 AAPL @ 145.00
BOUGHT 1 AAPL @ 130.00
--- Backtest Finished ---
Final Capital: $10,950.00
INFO  Python execution completed in 42.3 seconds
INFO  Parsing results...
INFO  Saving backtest to database...
INFO  Backtest saved with ID: 5
INFO  Saving 2 trades to database...
INFO  All done! Returning results to frontend
```

**In React, you'll see:**
```
╔════════════════════════════════════╗
║  Backtest Results                  ║
╠════════════════════════════════════╣
║  Starting Capital: $10,000.00      ║
║  Final Capital:    $10,950.00      ║
║  Profit/Loss:      +$950.00 (9.5%) ║
║  Total Trades:     2               ║
║                                    ║
║  Trade History:                    ║
║  2023-02-15  BUY   AAPL  $125.50   ║
║  2023-04-20  SELL  AAPL  $145.00   ║
╚════════════════════════════════════╝
```

### Test 4: Verify Data was Saved

Open a new terminal and check the database:

```bash
# Check backtest was saved
psql stockdsl -c "SELECT id, final_capital, profit_loss FROM backtests;"

# Should show:
#  id | final_capital | profit_loss
# ----+---------------+-------------
#  5  |      10950.00 |      950.00

# Check trades were saved
psql stockdsl -c "SELECT * FROM trades WHERE backtest_id = 5;"

# Should show:
#  id | backtest_id | trade_date | action | symbol | price
# ----+-------------+------------+--------+--------+--------
#  1  |           5 | 2023-02-15 | BUY    | AAPL   | 125.50
#  2  |           5 | 2023-04-20 | SELL   | AAPL   | 145.00
```

✅ **SUCCESS! The full flow works!**

---

## **Debugging Guide - When Things Go Wrong**

### Problem: Backend won't start

**Error:** `Cannot find or load main class`

**Solution:**
```bash
mvn clean install
```

### Problem: "Table does not exist"

**Error:** `ERROR: relation "users" does not exist`

**Solution:**
```bash
psql stockdsl < fullstack-app/database/schema.sql
```

### Problem: React shows "Network Error"

**Check:**
1. Is backend running? Go to http://localhost:8080
2. Check CORS - backend should allow localhost:3000
3. Check browser console for exact error

### Problem: Python execution fails

**Error:** `ModuleNotFoundError: No module named 'yahooquery'`

**Solution:**
```bash
pip3 install yahooquery pandas pandas_ta
```

### Problem: Can't login

**Check:**
1. Does demo user exist?
   ```bash
   psql stockdsl -c "SELECT * FROM users WHERE username='demo_user';"
   ```
2. Password is `password123`
3. Check backend logs for authentication errors

---

## **Advanced Testing with Postman**

For more detailed API testing, use Postman:

### Setup

1. Download Postman: https://www.postman.com/downloads/
2. Create a new Collection: "StockDSL API"

### Test Sequence

#### 1. Login
```
POST http://localhost:8080/api/auth/login
Body (JSON):
{
  "username": "demo_user",
  "password": "password123"
}
```

Save the token from response.

#### 2. Get Strategies
```
GET http://localhost:8080/api/strategies
Headers:
  Authorization: Bearer YOUR_TOKEN
```

#### 3. Create Strategy
```
POST http://localhost:8080/api/strategies
Headers:
  Authorization: Bearer YOUR_TOKEN
Body (JSON):
{
  "name": "My New Strategy",
  "dslCode": "strategy \"test\" { symbols: AAPL\n if rsi < 30 { buy AAPL } }",
  "description": "Testing from Postman"
}
```

#### 4. Run Backtest
```
POST http://localhost:8080/api/backtests/run/1
Headers:
  Authorization: Bearer YOUR_TOKEN
```

Wait ~30-60 seconds for response.

---

## **Watching the Data Flow Live**

### Backend Logs (Terminal 1)

```
2024-01-20 INFO  [http-nio-8080-exec-1] Received POST /api/strategies
2024-01-20 INFO  [http-nio-8080-exec-1] User: demo_user
2024-01-20 INFO  [http-nio-8080-exec-1] Creating strategy: My New Strategy
2024-01-20 INFO  [http-nio-8080-exec-1] Strategy saved with ID: 3
2024-01-20 INFO  [http-nio-8080-exec-1] Returning 200 OK
```

### Database (Terminal 3)

Watch database changes live:
```bash
# In one terminal, watch the strategies table
watch -n 2 'psql stockdsl -c "SELECT id, name, created_at FROM strategies;"'
```

Every 2 seconds, it refreshes. When you create a strategy in React, you'll see it appear!

### Browser Network Tab (DevTools)

1. Open DevTools (`F12`)
2. Go to "Network" tab
3. Click "Fetch/XHR" filter
4. Do an action (create strategy, run backtest)
5. Click the request to see:
   - Request Headers (including JWT token)
   - Request Body (the DSL code you sent)
   - Response (the results)

---

## **Success Checklist**

Test each of these in order:

- [ ] Your original ANTLR code compiles and runs
- [ ] PostgreSQL is installed and running
- [ ] Database `stockdsl` exists
- [ ] Tables created (users, strategies, backtests, trades)
- [ ] Demo user exists in database
- [ ] Backend starts without errors
- [ ] Can login via curl and get JWT token
- [ ] Can get strategies via API with token
- [ ] Frontend starts and opens in browser
- [ ] Can login in React UI
- [ ] Can see list of strategies
- [ ] Can create new strategy
- [ ] Can run backtest
- [ ] Results appear in UI
- [ ] Results saved to database

If all checked ✅ - **YOUR FULLSTACK APP IS WORKING!** 🎉

---

## **Next Steps**

Once everything works:

1. **Experiment** - Try different DSL strategies
2. **Break it** - Change code, see what happens
3. **Add features** - More indicators, better UI
4. **Deploy** - Put it on AWS, Heroku, etc.
5. **Show it off** - Add to resume, portfolio

---

## **Quick Commands Reference**

```bash
# Start Backend
cd fullstack-app/backend && mvn spring-boot:run

# Start Frontend
cd fullstack-app/frontend && npm start

# Check Database
psql stockdsl -c "SELECT * FROM strategies;"

# Test API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_user","password":"password123"}'

# Watch Logs
tail -f backend/logs/spring.log
```

---

**Happy Testing!** 🚀
