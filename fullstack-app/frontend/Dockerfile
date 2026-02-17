# ============================================================
# StockDSL Frontend Dockerfile
# ============================================================
#
# This Dockerfile creates a production build of the React app
# and serves it with nginx.
#
# Build: docker build -t stockdsl-frontend .
# Run:   docker run -p 3000:80 stockdsl-frontend
#
# ============================================================

# ============================================================
# STAGE 1: Build the React application
# ============================================================
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build argument for API URL (can be set at build time)
ARG REACT_APP_API_URL=http://localhost:8080/api
ENV REACT_APP_API_URL=$REACT_APP_API_URL

# Build the production bundle
RUN npm run build

# ============================================================
# STAGE 2: Serve with nginx
# ============================================================
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from builder stage
COPY --from=builder /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
