# Installing PostgreSQL on macOS

You need PostgreSQL for the fullstack app to store data.

## Option 1: Install with Homebrew (Recommended - 5 minutes)

### Step 1: Check if you have Homebrew

```bash
brew --version
```

**If you see a version number**, skip to Step 3.

**If you see "command not found"**, continue to Step 2.

### Step 2: Install Homebrew (if needed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Follow the instructions it gives you.

### Step 3: Install PostgreSQL

```bash
brew install postgresql@14
```

Wait 2-3 minutes for it to download and install.

### Step 4: Start PostgreSQL

```bash
brew services start postgresql@14
```

### Step 5: Add PostgreSQL to your PATH

```bash
echo 'export PATH="/opt/homebrew/opt/postgresql@14/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Step 6: Verify Installation

```bash
psql --version
```

You should see: `psql (PostgreSQL) 14.x`

### Step 7: Create Database

```bash
createdb stockdsl
psql stockdsl < fullstack-app/database/schema.sql
psql stockdsl -c "SELECT * FROM users;"
```

You should see the demo_user!

---

## Option 2: Install Postgres.app (Easiest - 2 minutes)

### Step 1: Download Postgres.app

1. Go to https://postgresapp.com/
2. Click "Download" (latest version)
3. Open the downloaded `.dmg` file
4. Drag Postgres to your Applications folder

### Step 2: Run Postgres.app

1. Open Applications folder
2. Double-click Postgres.app
3. Click "Initialize" (creates your first database)

### Step 3: Add to PATH

```bash
echo 'export PATH="/Applications/Postgres.app/Contents/Versions/latest/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Step 4: Verify

```bash
psql --version
```

### Step 5: Create Database

```bash
createdb stockdsl
psql stockdsl < fullstack-app/database/schema.sql
```

---

## Option 3: Use SQLite Instead (No Installation - 1 minute)

If you just want to test quickly without PostgreSQL, you can use SQLite temporarily.

### Modify Backend Configuration

Edit `fullstack-app/backend/src/main/resources/application.properties`:

**Change from:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdsl
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**To:**
```properties
spring.datasource.url=jdbc:sqlite:stockdsl.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=create
```

### Add SQLite Dependency

Edit `fullstack-app/backend/pom.xml` and add inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.42.0.0</version>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>
```

Then run:
```bash
cd fullstack-app/backend
mvn clean install
mvn spring-boot:run
```

Spring Boot will automatically create the database file and tables!

**Note:** SQLite is simpler but less powerful than PostgreSQL. Good for learning, but use PostgreSQL for production.

---

## What I Recommend

**For learning:** Use **Postgres.app** (Option 2) - easiest GUI

**For serious development:** Use **Homebrew** (Option 1) - more control

**For quick testing:** Use **SQLite** (Option 3) - no installation

---

## After Installation

Once PostgreSQL is installed and running, continue with:

```bash
# Create database
createdb stockdsl

# Load schema
psql stockdsl < fullstack-app/database/schema.sql

# Verify
psql stockdsl -c "SELECT username FROM users;"
# Should show: demo_user
```

Then proceed to [README_TESTING.md](README_TESTING.md) Step 2!

---

## Troubleshooting

### "createdb: command not found"
- PostgreSQL is not in your PATH
- Run the PATH commands from above again
- Close and reopen your terminal

### "connection refused"
- PostgreSQL is not running
- Run: `brew services start postgresql@14`
- Or open Postgres.app

### "role does not exist"
- Need to create a user
- Run: `createuser -s postgres`

---

**Which option are you choosing?** Let me know if you need help with any step!
