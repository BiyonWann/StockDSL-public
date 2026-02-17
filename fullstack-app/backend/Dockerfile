# ============================================================
# StockDSL Backend Dockerfile
# ============================================================
#
# This Dockerfile creates a container with:
# - Java 17 (for Spring Boot)
# - Python 3 (for backtesting scripts)
# - Required Python packages (yahooquery, pandas, pandas_ta)
#
# Build: docker build -t stockdsl-backend .
# Run:   docker run -p 8080:8080 stockdsl-backend
#
# ============================================================

# ============================================================
# STAGE 1: Build the Java application
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven files first (for dependency caching)
COPY pom.xml .

# Download dependencies (this layer is cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# ============================================================
# STAGE 2: Create the runtime image
# ============================================================
FROM eclipse-temurin:17-jre

# Install Python and pip
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Create a virtual environment for Python packages
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Install Python packages required for backtesting
RUN pip install --no-cache-dir \
    yahooquery \
    pandas \
    pandas_ta \
    numpy

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create directory for temporary Python scripts
RUN mkdir -p /app/temp

# Environment variables (can be overridden at runtime)
ENV PORT=8080
ENV PYTHON_PATH=/opt/venv/bin/python3
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
# Using shell form to allow environment variable expansion
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
