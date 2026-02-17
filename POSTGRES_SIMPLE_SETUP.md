# PostgreSQL Simple Setup

Good news! PostgreSQL is already installed and running on your Mac!

The issue is it's asking for a password. Let's fix this quickly.

## Quick Fix (2 minutes)

### Option 1: Use Postgres.app (Easiest!)

1. **Download Postgres.app:**
   - Go to: https://postgresapp.com/
   - Click "Download"
   - Open the `.dmg` file
   - Drag Postgres to Applications

2. **Open Postgres.app:**
   - Open Applications folder
   - Double-click Postgres
   - Click "Initialize" if it asks

3. **Add to PATH (copy-paste this):**
   ```bash
   echo 'export PATH="/Applications/Postgres.app/Contents/Versions/latest/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

4. **Test:**
   ```bash
   createdb stockdsl
   ```

That's it! No password needed.

---

### Option 2: Fix Homebrew PostgreSQL (Advanced)

```bash
# Stop any running PostgreSQL
brew services stop postgresql@14

# Remove the data directory
rm -rf /opt/homebrew/var/postgresql@14

# Reinitialize with no password
/opt/homebrew/opt/postgresql@14/bin/initdb -D /opt/homebrew/var/postgresql@14 --auth=trust

# Start it
brew services start postgresql@14

# Add to PATH
export PATH="/opt/homebrew/opt/postgresql@14/bin:$PATH"
echo 'export PATH="/opt/homebrew/opt/postgresql@14/bin:$PATH"' >> ~/.zshrc

# Test
createdb stockdsl
```

---

### Option 3: Use SQLite (No PostgreSQL needed!)

The easiest way to test your app is to use SQLite instead of PostgreSQL.

**Edit:** `fullstack-app/backend/src/main/resources/application.properties`

Change:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdsl
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

To:
```properties
spring.datasource.url=jdbc:h2:mem:stockdsl
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create
```

**Edit:** `fullstack-app/backend/pom.xml`

Add this dependency:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Then just run:
```bash
cd fullstack-app/backend
mvn clean install
mvn spring-boot:run
```

Spring Boot will create an in-memory database automatically! No PostgreSQL needed!

---

## My Recommendation

**For quick testing:** Use Option 3 (H2 database) - works instantly!

**For learning:** Use Option 1 (Postgres.app) - easiest GUI

**For production:** Fix Homebrew PostgreSQL (Option 2)

---

## Next Steps After Database is Working

1. Create database:
   ```bash
   createdb stockdsl
   psql stockdsl < fullstack-app/database/schema.sql
   ```

2. Start backend:
   ```bash
   cd fullstack-app/backend
   mvn spring-boot:run
   ```

3. Start frontend:
   ```bash
   cd fullstack-app/frontend
   npm start
   ```

---

Let me know which option you want to try!
