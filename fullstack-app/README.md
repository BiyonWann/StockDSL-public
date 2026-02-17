# StockDSL Fullstack Web Application

**Author:** Biyon Wanninayake
**Tech Stack:** React, Spring Boot, PostgreSQL, ANTLR, Java, Python

---

## Project Overview

A web-based trading strategy platform where users can:
1. Write trading strategies in a custom domain-specific language (DSL)
2. Compile DSL code to Python using ANTLR grammar
3. Run backtests on historical stock data
4. View results, trade history, and performance metrics

---

## Directory Structure

```
fullstack-app/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/stockdsl/
│   │   ├── controller/               # REST API endpoints
│   │   │   ├── AuthController.java      # Login/signup
│   │   │   ├── StrategyController.java  # CRUD for strategies
│   │   │   └── BacktestController.java  # Run backtests
│   │   ├── service/                  # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── StrategyService.java
│   │   │   └── BacktestService.java
│   │   ├── model/                    # Database entities (JPA)
│   │   │   ├── User.java
│   │   │   ├── Strategy.java
│   │   │   ├── Backtest.java
│   │   │   └── Trade.java
│   │   ├── repository/               # Database access (JPA)
│   │   │   ├── UserRepository.java
│   │   │   ├── StrategyRepository.java
│   │   │   ├── BacktestRepository.java
│   │   │   └── TradeRepository.java
│   │   ├── compiler/                 # ANTLR integration
│   │   │   ├── Translator.java         # Your existing code
│   │   │   ├── StockDSLLexer.java      # ANTLR generated
│   │   │   ├── StockDSLParser.java     # ANTLR generated
│   │   │   └── CompilerService.java    # Wrapper service
│   │   ├── security/                 # Authentication
│   │   │   ├── JwtUtil.java            # JWT token generation
│   │   │   └── SecurityConfig.java     # Spring Security config
│   │   ├── config/                   # Configuration
│   │   │   └── CorsConfig.java         # CORS for React
│   │   └── StockDslApplication.java  # Main entry point
│   ├── src/main/resources/
│   │   ├── application.properties    # Database config
│   │   └── StockDSL.g4              # ANTLR grammar file
│   └── pom.xml                       # Maven dependencies
│
├── frontend/                         # React application
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/               # Reusable UI components
│   │   │   ├── CodeEditor.js           # DSL code editor
│   │   │   ├── StrategyList.js         # List of strategies
│   │   │   ├── BacktestResults.js      # Results display
│   │   │   ├── TradeHistory.js         # Trade table
│   │   │   └── Navbar.js               # Navigation bar
│   │   ├── pages/                    # Full pages
│   │   │   ├── Login.js                # Login page
│   │   │   ├── Signup.js               # Signup page
│   │   │   ├── Dashboard.js            # Main dashboard
│   │   │   ├── StrategyEditor.js       # Create/edit strategies
│   │   │   └── BacktestHistory.js      # View past backtests
│   │   ├── services/                 # API calls
│   │   │   ├── api.js                  # Axios HTTP client
│   │   │   ├── authService.js          # Login/signup API
│   │   │   └── strategyService.js      # Strategy API
│   │   ├── styles/                   # CSS files
│   │   │   └── App.css
│   │   ├── App.js                    # Main React component
│   │   └── index.js                  # React entry point
│   ├── package.json                  # npm dependencies
│   └── .env                          # Environment variables
│
├── database/
│   └── schema.sql                    # PostgreSQL schema
│
└── README.md                         # This file
```

---

## Technology Breakdown

### 1. **Frontend: React**
- **Purpose:** User interface
- **Key Libraries:**
  - `react-router-dom`: Page navigation
  - `axios`: HTTP requests to backend
  - `@monaco-editor/react`: Code editor (like VS Code)
  - `recharts`: Charts for results

### 2. **Backend: Spring Boot**
- **Purpose:** REST API server
- **Key Dependencies:**
  - `spring-boot-starter-web`: REST API
  - `spring-boot-starter-data-jpa`: Database ORM
  - `spring-boot-starter-security`: Authentication
  - `postgresql`: PostgreSQL driver
  - `jjwt`: JWT tokens
  - `antlr4-runtime`: Your DSL compiler

### 3. **Database: PostgreSQL**
- **Purpose:** Persistent data storage
- **Tables:**
  - `users`: User accounts
  - `strategies`: DSL code
  - `backtests`: Backtest results
  - `trades`: Individual buy/sell actions

### 4. **Compiler: Java/ANTLR**
- **Purpose:** Translate DSL → Python
- **Files:**
  - `StockDSL.g4`: Grammar definition
  - `Translator.java`: Visitor pattern implementation
  - ANTLR generates lexer/parser

### 5. **Execution: Python**
- **Purpose:** Run backtests
- **Libraries:**
  - `yahooquery`: Stock data
  - `pandas`: Data manipulation
  - `pandas_ta`: Technical indicators

---

## How Data Flows Through the System

### Example: User Runs a Backtest

```
1. User types DSL in React editor:
   ┌──────────────────────────────────────┐
   │ strategy "my_strategy" {             │
   │     symbols: NVDA                    │
   │     capital: $10000                  │
   │     if rsi < 30 { buy NVDA }         │
   │ }                                    │
   └──────────────────────────────────────┘

2. User clicks "Save Strategy"
   → React sends POST /api/strategies
   → Spring Boot saves to PostgreSQL (strategies table)

3. User clicks "Run Backtest"
   → React sends POST /api/backtests/run/{strategyId}

4. Spring Boot (BacktestService):
   a) Fetches strategy from database
   b) Passes DSL code to CompilerService
   c) CompilerService uses YOUR Translator.java
   d) Generates Python code

5. Spring Boot executes Python:
   → ProcessBuilder runs Python script
   → Python downloads NVDA prices from Yahoo Finance
   → Python simulates trades day-by-day
   → Returns results as JSON

6. Spring Boot saves results:
   → Parses Python output
   → Saves to backtests table (summary)
   → Saves to trades table (individual trades)

7. Spring Boot returns JSON to React:
   {
     "finalCapital": 11500,
     "profitLoss": 1500,
     "trades": [...]
   }

8. React displays results:
   → Shows profit/loss
   → Renders trade history table
   → Creates chart
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Strategies Table
```sql
CREATE TABLE strategies (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    dsl_code TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);
```

### Backtests Table
```sql
CREATE TABLE backtests (
    id SERIAL PRIMARY KEY,
    strategy_id INT REFERENCES strategies(id),
    user_id INT REFERENCES users(id),
    final_capital DECIMAL(15, 2),
    profit_loss DECIMAL(15, 2),
    profit_loss_percent DECIMAL(5, 2),
    total_trades INT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Trades Table
```sql
CREATE TABLE trades (
    id SERIAL PRIMARY KEY,
    backtest_id INT REFERENCES backtests(id),
    trade_date DATE NOT NULL,
    action VARCHAR(10) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT DEFAULT 1
);
```

---

## REST API Endpoints

### Authentication
- `POST /api/auth/signup` - Create new user
- `POST /api/auth/login` - Login, get JWT token

### Strategies
- `GET /api/strategies` - Get all user's strategies
- `GET /api/strategies/{id}` - Get specific strategy
- `POST /api/strategies` - Create new strategy
- `PUT /api/strategies/{id}` - Update strategy
- `DELETE /api/strategies/{id}` - Delete strategy

### Backtests
- `POST /api/backtests/run/{strategyId}` - Run backtest
- `GET /api/backtests/strategy/{strategyId}` - Get backtest history
- `GET /api/backtests/{id}` - Get specific backtest
- `GET /api/backtests/{id}/trades` - Get trades for backtest

---

## Setup Instructions

### Prerequisites
- Java 11+
- Node.js 16+
- PostgreSQL 13+
- Python 3.8+
- Maven

### 1. Database Setup
```bash
# Create database
createdb stockdsl

# Run schema
psql stockdsl < database/schema.sql
```

### 2. Backend Setup
```bash
cd backend

# Install dependencies
mvn clean install

# Configure database in src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdsl
spring.datasource.username=your_username
spring.datasource.password=your_password

# Run Spring Boot
mvn spring-boot:run
# Backend runs on http://localhost:8080
```

### 3. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Configure backend URL in .env
REACT_APP_API_URL=http://localhost:8080

# Run React
npm start
# Frontend runs on http://localhost:3000
```

### 4. Test the App
1. Open http://localhost:3000
2. Sign up for an account
3. Create a strategy
4. Click "Run Backtest"
5. View results!

---

## Key Features

✅ **User Authentication** - JWT-based login/signup
✅ **Strategy Management** - Create, edit, delete strategies
✅ **DSL Compilation** - ANTLR translates DSL → Python
✅ **Live Backtesting** - Runs on real historical data
✅ **Results Visualization** - Charts and tables
✅ **Trade History** - Detailed buy/sell records
✅ **Multi-Strategy Support** - Manage multiple strategies
✅ **History Tracking** - View past backtest runs

---

## Technical Challenges & Solutions

### Challenge 1: Integrating ANTLR with Spring Boot
**Problem:** Your ANTLR code reads from files, but web apps need to process strings from HTTP requests.

**Solution:** Modified the compiler to accept strings:
```java
// Old: CharStream input = CharStreams.fromFileName(dslFilePath);
// New: CharStream input = CharStreams.fromString(dslCode);
```

### Challenge 2: Running Python from Java
**Problem:** Python execution is synchronous and blocks the thread.

**Solution:** Use ProcessBuilder and capture output:
```java
ProcessBuilder pb = new ProcessBuilder("python", "-c", pythonCode);
Process process = pb.start();
BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
```

### Challenge 3: User Authentication
**Problem:** Need secure login without session management.

**Solution:** JWT tokens - stateless, stored in localStorage, sent with every request.

### Challenge 4: Parsing Python Output
**Problem:** Python prints text, need structured data.

**Solution:** Modify Python to output JSON instead of print statements.

---

## Future Enhancements

- 📊 Add more technical indicators (MACD, Bollinger Bands)
- 🔄 Real-time paper trading
- 📱 Mobile responsive design
- 🤝 Share strategies publicly
- 📈 Compare multiple strategies side-by-side
- ☁️ Deploy to AWS (S3, RDS, EC2)
- 🐳 Docker containerization

---

## Learning Outcomes

By building this project, you'll learn:
- ✅ Fullstack development (React + Spring Boot)
- ✅ RESTful API design
- ✅ Database modeling (PostgreSQL)
- ✅ User authentication (JWT)
- ✅ Compiler design (ANTLR)
- ✅ Integration of multiple technologies
- ✅ Real-world software architecture

---

## How to Explain This Project

**For Interviews:**
> "I built a fullstack trading platform where users write strategies in a custom DSL. The React frontend provides a code editor and results dashboard. The Spring Boot backend exposes REST APIs for strategy management and backtest execution. I integrated my existing ANTLR-based compiler to translate DSL to Python, which downloads real stock data and simulates trades. Results are persisted in PostgreSQL for historical analysis. The architecture demonstrates separation of concerns, with clear layers for UI, business logic, compilation, and data persistence."

**For Professors:**
> "This project combines concepts from multiple CS courses: software design (MVC architecture), databases (relational modeling), programming languages (ANTLR parsing), and web development (REST APIs). The system translates a domain-specific language to executable Python code, demonstrating compiler theory in practice. User authentication with JWT shows security concepts, and the React frontend demonstrates modern UI development."

**For Your Resume:**
> "Designed a domain-specific language for expressing algorithmic stock trading strategies. Implemented ANTLR grammar and visitor parsing to support technical indicators and logical operators. Built a translator that compiles the language into Python backtests executable on historical market data. Created a React strategy editor backed by Spring Boot REST API and PostgreSQL, supporting version history."

---

## Questions You Might Get Asked

**Q: Why use ANTLR instead of just parsing strings manually?**
A: ANTLR generates a robust parser from grammar rules, handles edge cases, provides error reporting, and follows compiler theory best practices. Manual parsing would be error-prone.

**Q: Why Spring Boot instead of Node.js for the backend?**
A: My DSL compiler is written in Java with ANTLR. Spring Boot lets me integrate this existing code seamlessly without rewriting in JavaScript.

**Q: Why store backtest results in the database?**
A: Backtests are expensive (20-60 seconds). Storing results lets users view historical runs instantly without re-running. Also enables analytics like comparing strategy performance over time.

**Q: How do you ensure user's can't see each other's strategies?**
A: Every strategy has a `user_id` foreign key. All database queries filter by the authenticated user's ID. JWT tokens identify which user is making requests.

**Q: What if the Python execution takes too long?**
A: Currently synchronous (blocks the thread). Future improvement: use asynchronous execution with WebSockets to stream progress updates to the frontend.

---

## License
Educational project for portfolio purposes.
