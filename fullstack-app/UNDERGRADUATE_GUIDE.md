# StockDSL Fullstack - Undergraduate Developer Guide

**Written for CS students learning fullstack development**

---

## Table of Contents

1. [What This Project Is](#what-this-project-is)
2. [Why These Technologies](#why-these-technologies)
3. [How the Pieces Connect](#how-the-pieces-connect)
4. [Understanding Each Layer](#understanding-each-layer)
5. [The Request Flow](#the-request-flow)
6. [Interview Preparation](#interview-preparation)
7. [Common Questions & Answers](#common-questions--answers)

---

## What This Project Is

A **fullstack web application** for creating and testing stock trading strategies.

**The Problem It Solves:**
Writing trading strategies usually requires complex Python code with pandas, technical indicators, and data fetching. This is hard for non-programmers.

**Your Solution:**
A custom language (DSL) that's simple:
```
if rsi < 30 { buy AAPL }
```

Instead of complex Python:
```python
df['RSI'] = df.ta.rsi(close=df['close'], length=14)
for i in range(len(df)):
    if df['RSI'].iloc[i] < 30:
        portfolio.buy('AAPL', df['close'].iloc[i])
```

---

## Why These Technologies

### React (Frontend)
**What:** JavaScript library for building UIs
**Why:**
- Industry standard (used by Facebook, Netflix, Airbnb)
- Component-based (reusable code)
- Fast rendering (Virtual DOM)
- Huge ecosystem

**Alternatives:** Vue.js, Angular, Svelte
**Why React?** Most popular, best job market

### Spring Boot (Backend)
**What:** Java framework for building REST APIs
**Why:**
- Your ANTLR compiler is in Java
- Enterprise-grade (used by banks, Fortune 500)
- Built-in security, database integration
- Auto-configuration (less boilerplate)

**Alternatives:** Express.js (Node), Django (Python), Flask (Python)
**Why Spring Boot?** Integrates perfectly with your existing Java code

### PostgreSQL (Database)
**What:** Relational database (SQL)
**Why:**
- Reliable and mature
- ACID compliant (data integrity)
- Great for structured data (users, strategies, results)
- Free and open-source

**Alternatives:** MySQL, MongoDB, SQLite
**Why PostgreSQL?** Best open-source relational DB

### ANTLR (Parser Generator)
**What:** Tool for creating parsers from grammar files
**Why:**
- Industry standard for DSLs
- Automatically generates lexer/parser
- Used by Twitter, Oracle, Google

**Alternatives:** JavaCC, Bison, YACC
**Why ANTLR?** Most modern, best documentation

---

## How the Pieces Connect

### The Stack (Bottom to Top)

```
┌─────────────────────────────────────┐
│  Browser (Chrome, Firefox, etc.)    │  ← What the user sees
│  React App (JavaScript)             │
└──────────────┬──────────────────────┘
               │ HTTP (JSON)
               ↓
┌─────────────────────────────────────┐
│  Spring Boot Server (Java)          │  ← Your API
│  - REST Controllers                 │
│  - Business Logic (Services)        │
│  - Your ANTLR Compiler              │
└──────────────┬──────────────────────┘
               │ JDBC (SQL)
               ↓
┌─────────────────────────────────────┐
│  PostgreSQL Database                │  ← Data storage
│  - users table                      │
│  - strategies table                 │
│  - backtests table                  │
│  - trades table                     │
└─────────────────────────────────────┘
```

### Communication Protocol

**Frontend ↔ Backend:** REST API (HTTP + JSON)
- Frontend: `POST /api/strategies { name: "My Strategy", dslCode: "..." }`
- Backend: `{ id: 42, name: "My Strategy", createdAt: "..." }`

**Backend ↔ Database:** JDBC (Java Database Connectivity)
- Java code: `strategyRepository.findByUserId(userId)`
- PostgreSQL: `SELECT * FROM strategies WHERE user_id = ?`

**Backend ↔ Python:** Process Execution
- Java: `ProcessBuilder("python3", "-c", pythonCode)`
- Python: Runs, prints output
- Java: Captures output as String

---

## Understanding Each Layer

### Layer 1: Database (PostgreSQL)

**What it stores:**
```sql
users
├─ id, username, email, password_hash
│
strategies (belongs to user)
├─ id, user_id, name, dsl_code
│
backtests (belongs to strategy and user)
├─ id, strategy_id, user_id, final_capital, profit_loss
│
trades (belongs to backtest)
└─ id, backtest_id, date, action, symbol, price
```

**Relationships:**
- One user → Many strategies
- One strategy → Many backtests
- One backtest → Many trades

**Think of it like:**
- **Users** = Your Google account
- **Strategies** = Your saved Google Docs
- **Backtests** = Different versions of a Doc
- **Trades** = Individual edits in a version

### Layer 2: Backend (Spring Boot)

**Structure:**
```
Controllers (Handle HTTP requests)
    ↓
Services (Business logic)
    ↓
Repositories (Database queries)
    ↓
PostgreSQL
```

**Example Flow:**
```java
// 1. Controller receives HTTP request
@PostMapping("/api/strategies")
public Strategy create(@RequestBody StrategyRequest req) {
    return strategyService.create(req);  // Call service
}

// 2. Service contains business logic
public Strategy create(StrategyRequest req) {
    // Validate
    if (req.name.isEmpty()) throw new Exception("Name required");

    // Create entity
    Strategy strategy = new Strategy();
    strategy.setName(req.name);

    // Save to database
    return strategyRepository.save(strategy);  // Call repository
}

// 3. Repository talks to database
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    // Spring auto-generates: INSERT INTO strategies ...
}
```

**Key Concept: MVC Pattern**
- **Model:** Database entities (User, Strategy)
- **View:** React frontend (separate from backend)
- **Controller:** REST endpoints

### Layer 3: Compiler (ANTLR + Your Code)

**Flow:**
```
DSL String
    ↓
Lexer (tokenize)
    ↓
Parser (build tree)
    ↓
Your Translator (visit tree)
    ↓
Python String
```

**Example:**
```java
// Input DSL
String dsl = "if rsi < 30 { buy AAPL }";

// Step 1: Lexer breaks into tokens
// [IF, RSI, LESS_THAN, NUMBER(30), LBRACE, BUY, SYMBOL(AAPL), RBRACE]

// Step 2: Parser builds tree
//         ifStatement
//        /           \
//   condition      action
//   rsi < 30      buy AAPL

// Step 3: Your Translator walks tree
public String visitIfStatement(IfContext ctx) {
    String condition = visit(ctx.condition());  // "rsi < 30"
    String action = visit(ctx.action());        // "buy('AAPL')"
    return "if " + condition + ":\n    " + action;
}

// Output Python
"if rsi < 30:\n    portfolio.buy('AAPL', price)"
```

### Layer 4: Frontend (React)

**Component Structure:**
```
App (routing)
├─ Login (page)
├─ Signup (page)
├─ Dashboard (page)
│   ├─ Navbar (component)
│   └─ StrategyList (component)
└─ StrategyEditor (page)
    ├─ CodeEditor (component)
    └─ BacktestResults (component)
```

**Example Component:**
```javascript
// React component = function that returns HTML
function StrategyList() {
  const [strategies, setStrategies] = useState([]);

  // Load data when component mounts
  useEffect(() => {
    fetch('/api/strategies')
      .then(res => res.json())
      .then(data => setStrategies(data));
  }, []);

  // Render UI
  return (
    <div>
      <h2>My Strategies</h2>
      {strategies.map(s => (
        <div key={s.id}>{s.name}</div>
      ))}
    </div>
  );
}
```

**Key Concepts:**
- **useState:** Store data in component
- **useEffect:** Run code when component loads
- **Props:** Pass data between components
- **JSX:** HTML-like syntax in JavaScript

---

## The Request Flow

### Example: User Runs a Backtest

**Step 1:** User clicks "Run Backtest" button in React

```javascript
// React component
const runBacktest = async () => {
  const response = await fetch(`/api/backtests/run/${strategyId}`, {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  const results = await response.json();
  setResults(results);
};
```

**Step 2:** HTTP Request sent to Spring Boot

```
POST http://localhost:8080/api/backtests/run/42
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
```

**Step 3:** Spring Boot Controller receives request

```java
@PostMapping("/backtests/run/{strategyId}")
public BacktestResult runBacktest(@PathVariable Long strategyId, Authentication auth) {
    String username = auth.getName();  // Extract from JWT
    return backtestService.run(strategyId, username);
}
```

**Step 4:** Service loads strategy from database

```java
Strategy strategy = strategyRepository.findById(strategyId)
    .orElseThrow(() -> new NotFoundException());

// Check user owns this strategy
if (!strategy.getUser().getUsername().equals(username)) {
    throw new UnauthorizedException();
}

String dslCode = strategy.getDslCode();
```

**Step 5:** Service calls compiler

```java
CompilerService.CompilationResult compilation = compilerService.compile(dslCode);

if (!compilation.success) {
    throw new CompilationException(compilation.errorMessage);
}

String pythonCode = compilation.pythonCode;
```

**Step 6:** Compiler translates DSL → Python

```java
// Your Translator.java
CharStream input = CharStreams.fromString(dslCode);
StockDSLLexer lexer = new StockDSLLexer(input);
StockDSLParser parser = new StockDSLParser(new CommonTokenStream(lexer));
ParseTree tree = parser.program();

Translator translator = new Translator();
String pythonCode = translator.visit(tree);
// Returns: "import yahooquery\n..."
```

**Step 7:** Service executes Python

```java
ExecutionResult result = compilerService.executePython(pythonCode);
List<String> outputLines = result.output;
// ["--- Starting Backtest ---", "BOUGHT 1 AAPL @ 150", ...]
```

**Step 8:** Python downloads stock data and simulates trading

```python
# Generated Python code
ticker = yq.Ticker(['AAPL'])
data = ticker.history(start='2023-01-01', end='2023-12-31')

# Calculate RSI
data['RSI'] = data.ta.rsi(close=data['close'], length=14)

# Simulate trading
for i in range(len(data)):
    if data['RSI'].iloc[i] < 30:
        portfolio.buy('AAPL', data['close'].iloc[i])

# Print results
print(f"Final Capital: ${portfolio.capital}")
```

**Step 9:** Service parses Python output

```java
BacktestResult result = compilerService.parseBacktestOutput(outputLines);
// result.finalCapital = 11500.0
// result.profitLoss = 1500.0
// result.trades = ["BOUGHT 1 AAPL @ 150", "SOLD 1 AAPL @ 165"]
```

**Step 10:** Service saves to database

```java
Backtest backtest = new Backtest();
backtest.setStrategy(strategy);
backtest.setUser(user);
backtest.setFinalCapital(result.finalCapital);
backtest.setProfitLoss(result.profitLoss);
backtestRepository.save(backtest);

// Save each trade
for (String tradeStr : result.trades) {
    Trade trade = parseTradeString(tradeStr);
    trade.setBacktest(backtest);
    tradeRepository.save(trade);
}
```

**Step 11:** Service returns results to Controller

```java
return result;  // Returns BacktestResult object
```

**Step 12:** Spring Boot serializes to JSON

```json
{
  "finalCapital": 11500.0,
  "profitLoss": 1500.0,
  "profitLossPercent": 15.0,
  "totalTrades": 2,
  "trades": [
    "BOUGHT 1 AAPL @ 150.00",
    "SOLD 1 AAPL @ 165.00"
  ]
}
```

**Step 13:** React receives JSON and updates UI

```javascript
const results = await response.json();
setResults(results);  // Triggers re-render

// React displays:
// Final Capital: $11,500
// Profit: +$1,500 (15%)
```

**Total time:** ~30-60 seconds (mostly waiting for Yahoo Finance data)

---

## Interview Preparation

### Technical Questions You Might Get

**Q: Explain your architecture.**

A: "Three-tier architecture. React frontend for UI, Spring Boot backend for business logic and API, PostgreSQL for data persistence. Frontend and backend communicate via REST API with JSON. Backend integrates my ANTLR compiler to translate DSL to Python, which executes via ProcessBuilder."

**Q: Why not do everything in JavaScript/Node.js?**

A: "My DSL compiler is Java-based using ANTLR, which has excellent Java support. Spring Boot provides seamless integration. Also, Spring Boot is enterprise-grade with built-in security, validation, and ORM, which would require more setup in Node.js."

**Q: How do you handle authentication?**

A: "JWT (JSON Web Tokens). User logs in, server validates credentials and returns a signed JWT. Frontend stores it in localStorage and includes it in the Authorization header of all requests. Backend verifies the signature and extracts the username to identify the user."

**Q: What's the security risk of executing user code?**

A: "Users don't write arbitrary Python - they write DSL that's translated to Python. The DSL grammar only allows trading operations (buy/sell) and indicators (RSI, SMA). No file I/O, network requests, or system commands. The Python execution is also sandboxed."

**Q: How do you prevent SQL injection?**

A: "Spring Data JPA uses parameterized queries automatically. We never concatenate user input into SQL strings. JPA translates method calls like `findByUsername(username)` into safe prepared statements."

**Q: What's the hardest technical challenge?**

A: "Integrating the ANTLR compiler with Spring Boot. My original code read from files, but web apps need to process strings from HTTP requests. I modified CharStreams.fromFileName() to CharStreams.fromString() and wrapped everything in a Spring service."

### Behavioral Questions

**Q: Why did you build this?**

A: "I wanted to learn fullstack development and apply compiler theory from my PL course. Trading strategies seemed like a perfect use case for a DSL - simple syntax for non-programmers but powerful enough for real backtesting."

**Q: What would you improve?**

A: "Async backtesting with WebSockets for progress updates, more technical indicators, better error messages with line numbers, mobile-responsive design, and deployment to AWS with Docker containerization."

---

## Common Questions & Answers

### General Architecture

**Q: Why separate frontend and backend?**
A: Separation of concerns. Frontend focuses on UI, backend on business logic. Can deploy independently, scale separately, and swap technologies easily.

**Q: What if Python takes 5 minutes to run?**
A: Add a job queue (RabbitMQ or Redis). Backend immediately returns "Job started" and processes async. Use WebSockets to push updates to frontend.

**Q: Can multiple users run backtests simultaneously?**
A: Yes, Spring Boot handles concurrent requests. Each backtest runs in its own process. For heavy load, add a job queue.

### Database

**Q: Why PostgreSQL instead of MongoDB?**
A: Data is relational (users have strategies, strategies have backtests). SQL is perfect for this. MongoDB is better for unstructured/document data.

**Q: What's a foreign key?**
A: Links between tables. `strategies.user_id` references `users.id`. Ensures a strategy always belongs to a valid user. Database enforces this.

**Q: Why store trades separately?**
A: One backtest has many trades. Storing in separate table (normalization) avoids data duplication and allows querying trades independently.

### Backend

**Q: What's Spring Boot auto-configuration?**
A: Detects libraries on classpath and automatically configures them. Add PostgreSQL driver → Spring auto-configures connection pool, transaction manager, etc.

**Q: What's JPA?**
A: Java Persistence API. Maps Java objects to database tables. `@Entity` class → table, object → row, field → column. Handles SQL automatically.

**Q: What's a DTO?**
A: Data Transfer Object. Simple class for transferring data between layers. Example: `LoginRequest { username, password }` vs. `User` entity (which has more fields).

### Frontend

**Q: What's the Virtual DOM?**
A: React's optimization. Instead of directly updating browser DOM (slow), React updates a virtual copy (fast) and calculates minimal changes needed.

**Q: What's JSX?**
A: JavaScript XML. HTML-like syntax in JavaScript. `<div>Hello</div>` compiles to `React.createElement('div', null, 'Hello')`.

**Q: Why use state management?**
A: Components need to store data (strategies list, user info). `useState` lets components remember data between renders.

### Compiler

**Q: What's ANTLR?**
A: Parser generator. You write grammar (`.g4` file), ANTLR generates lexer/parser Java code. Handles hard parts of parsing.

**Q: What's the visitor pattern?**
A: Design pattern for traversing trees. Each node type has a `visit` method. Your `Translator` extends `BaseVisitor` and overrides methods to generate Python.

**Q: Why not just use regex?**
A: Regex can't handle nested structures or complex grammars. ANTLR builds a proper abstract syntax tree and handles precedence, associativity, etc.

---

## Key Takeaways

1. **Fullstack = Frontend + Backend + Database**
   - Each layer has a specific responsibility
   - Communicate via well-defined interfaces (REST API, SQL)

2. **Your DSL Compiler is the Core**
   - Everything else supports it (web UI, data storage)
   - Spring Boot wraps it in a web service

3. **Real-World Architecture**
   - This is how professional apps are built
   - You've touched: compilers, databases, web dev, APIs, security

4. **Explainable to Anyone**
   - Non-technical: "A website for testing trading strategies"
   - Technical: "Fullstack platform with custom DSL compiler"
   - Expert: "ANTLR-based DSL translating to Python with Spring Boot REST API and React frontend"

---

**You've built something impressive. Own it in interviews!** 🚀
