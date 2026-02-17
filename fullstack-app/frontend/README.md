# StockDSL Frontend

React-based web interface for the StockDSL trading platform.

## Features

- 🔐 User authentication (login/signup)
- 📝 Code editor for writing DSL strategies
- ▶️ Run backtests with real-time results
- 📊 Visualize backtest results with charts
- 📜 View trade history
- 💾 Save and manage multiple strategies

## Tech Stack

- **React 18** - UI framework
- **React Router** - Page navigation
- **Axios** - HTTP requests to backend
- **Monaco Editor** - VS Code-style code editor
- **Recharts** - Charts and visualizations

## Setup

### Prerequisites

- Node.js 16+ and npm

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm start
```

The app will open at http://localhost:3000

## Project Structure

```
frontend/
├── public/
│   ├── index.html              # HTML template
│   └── favicon.ico
├── src/
│   ├── components/             # Reusable UI components
│   │   ├── Navbar.js              # Navigation bar
│   │   ├── CodeEditor.js          # DSL code editor
│   │   ├── StrategyList.js        # List of strategies
│   │   ├── BacktestResults.js     # Results display
│   │   └── TradeHistory.js        # Trade table
│   ├── pages/                  # Full page components
│   │   ├── Login.js               # Login page
│   │   ├── Signup.js              # Signup page
│   │   ├── Dashboard.js           # Main dashboard
│   │   ├── StrategyEditor.js      # Create/edit strategy
│   │   └── BacktestHistory.js     # View past backtests
│   ├── services/               # API communication
│   │   ├── api.js                 # Axios setup
│   │   ├── authService.js         # Login/signup
│   │   └── strategyService.js     # Strategy CRUD
│   ├── styles/                 # CSS files
│   │   └── App.css
│   ├── App.js                  # Main component with routing
│   └── index.js                # React entry point
├── package.json                # npm dependencies
└── .env                        # Environment variables
```

## Key Components

### 1. CodeEditor Component
Uses Monaco Editor (same as VS Code) for syntax highlighting and code editing.

### 2. Dashboard Page
Main page showing:
- List of user's strategies
- Quick stats
- Recent backtest results

### 3. StrategyEditor Page
- Code editor for writing DSL
- Save/update strategy
- Run backtest button
- View results inline

### 4. BacktestResults Component
Displays:
- Profit/loss summary
- Trade history table
- Performance chart

## API Integration

All API calls go through services:

```javascript
// Example: Login
import authService from './services/authService';

authService.login('username', 'password')
  .then(token => {
    // Store token, redirect to dashboard
  });

// Example: Get strategies
import strategyService from './services/strategyService';

strategyService.getAll()
  .then(strategies => {
    // Display strategies
  });
```

## User Flow

1. **Signup/Login**
   - User enters credentials
   - Get JWT token from backend
   - Store in localStorage
   - Redirect to dashboard

2. **Create Strategy**
   - Click "New Strategy"
   - Write DSL code in editor
   - Click "Save"
   - POST to /api/strategies

3. **Run Backtest**
   - Click "Run Backtest" on a strategy
   - Show loading spinner
   - POST to /api/backtests/run/{id}
   - Wait ~30 seconds
   - Display results

4. **View Results**
   - Show profit/loss
   - Render trade table
   - Create chart
   - Option to export CSV

## Development Tips

### Hot Reload
Changes to React files auto-reload the page.

### Testing API
Use browser DevTools Network tab to see API requests.

### Debugging
Add `console.log()` or use React DevTools extension.

### Building for Production

```bash
npm run build
```

Creates optimized build in `build/` folder.

## Common Issues

### Issue: CORS Error
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solution:** Backend must enable CORS for http://localhost:3000

### Issue: 401 Unauthorized
```
Request failed with status code 401
```
**Solution:** JWT token expired or invalid. Re-login.

### Issue: Cannot connect to backend
```
Network Error
```
**Solution:** Ensure backend is running on http://localhost:8080

## Next Steps

1. Add loading states for better UX
2. Add form validation
3. Add error handling
4. Add success/error notifications
5. Make responsive for mobile
6. Deploy to cloud (Vercel, Netlify, AWS S3)
