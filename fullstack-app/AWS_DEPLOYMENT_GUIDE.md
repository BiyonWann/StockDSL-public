# AWS Deployment Guide for StockDSL

This guide walks you through deploying StockDSL to AWS step by step.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [AWS Concepts You Need to Know](#aws-concepts-you-need-to-know)
3. [Architecture Overview](#architecture-overview)
4. [Step 1: Set Up AWS Account](#step-1-set-up-aws-account)
5. [Step 2: Deploy Database (RDS)](#step-2-deploy-database-rds)
6. [Step 3: Deploy Backend (Elastic Beanstalk)](#step-3-deploy-backend-elastic-beanstalk)
7. [Step 4: Deploy Frontend (S3 + CloudFront)](#step-4-deploy-frontend-s3--cloudfront)
8. [Step 5: Set Up Domain (Route 53)](#step-5-set-up-domain-route-53)
9. [Cost Optimization](#cost-optimization)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting, you need:

- [ ] AWS Account (free tier available)
- [ ] AWS CLI installed (`brew install awscli` on Mac)
- [ ] Docker installed (for building images)
- [ ] Domain name (optional, ~$12/year)

### Install AWS CLI

```bash
# Mac
brew install awscli

# Windows (download installer)
# https://aws.amazon.com/cli/

# Verify installation
aws --version
```

### Configure AWS CLI

```bash
# This will prompt for your AWS credentials
aws configure

# Enter:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region (e.g., us-east-1)
# - Default output format (json)
```

---

## AWS Concepts You Need to Know

### 1. **Regions and Availability Zones**
- **Region**: Geographic location (us-east-1 = N. Virginia)
- **Availability Zone**: Data center within a region (us-east-1a, us-east-1b)
- **Tip**: Choose a region close to your users

### 2. **IAM (Identity and Access Management)**
- Controls WHO can access WHAT in your AWS account
- **Users**: People or services
- **Roles**: Temporary permissions (used by services)
- **Policies**: Rules that define permissions

### 3. **VPC (Virtual Private Cloud)**
- Your private network in AWS
- Contains subnets, security groups, etc.
- AWS creates a default VPC for you

### 4. **Security Groups**
- Virtual firewalls for your resources
- Control inbound/outbound traffic
- Example: "Allow port 5432 from my backend only"

### 5. **Key Services**

| Service | What It Does | Analogy |
|---------|-------------|---------|
| **EC2** | Virtual servers | Renting a computer |
| **RDS** | Managed databases | Database as a service |
| **S3** | File storage | Dropbox for servers |
| **CloudFront** | CDN (content delivery) | Netflix for your files |
| **Elastic Beanstalk** | App deployment | Heroku on AWS |
| **Route 53** | DNS management | Domain name service |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│                                                                  │
│   Users                                                          │
│     │                                                            │
│     ▼                                                            │
│  ┌─────────────┐     ┌──────────────────────────────────────┐   │
│  │  Route 53   │────▶│          CloudFront (CDN)            │   │
│  │  (DNS)      │     │   stockdsl.com → S3 bucket           │   │
│  └─────────────┘     └──────────────────────────────────────┘   │
│                                      │                           │
│                                      ▼                           │
│                      ┌──────────────────────────────────────┐   │
│                      │            S3 Bucket                  │   │
│                      │   (React build files)                 │   │
│                      └──────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────┐     ┌──────────────────────────────────────┐   │
│  │ Application │     │      Elastic Beanstalk               │   │
│  │ Load        │────▶│   (Spring Boot + Python)             │   │
│  │ Balancer    │     │   api.stockdsl.com                   │   │
│  └─────────────┘     └──────────────────────────────────────┘   │
│                                      │                           │
│                                      ▼                           │
│                      ┌──────────────────────────────────────┐   │
│                      │            RDS PostgreSQL             │   │
│                      │   (Database)                          │   │
│                      └──────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step 1: Set Up AWS Account

### 1.1 Create AWS Account
1. Go to https://aws.amazon.com
2. Click "Create an AWS Account"
3. Enter email, password, account name
4. Add payment method (won't be charged if using free tier)
5. Verify phone number
6. Choose "Basic Support" (free)

### 1.2 Enable MFA (Multi-Factor Authentication)
**IMPORTANT**: Protect your account!

1. Go to IAM Console
2. Click your username → Security credentials
3. Enable MFA with authenticator app

### 1.3 Create IAM User (Don't use root account!)

```bash
# Or do this in AWS Console:
# IAM → Users → Add user
# - Username: stockdsl-admin
# - Access type: Programmatic access + Console access
# - Attach policy: AdministratorAccess (for learning; restrict in production)
```

### 1.4 Set Up Billing Alerts

1. Go to Billing Dashboard
2. Budgets → Create budget
3. Set a $10 or $20 budget
4. Get email alerts when approaching limit

---

## Step 2: Deploy Database (RDS)

### 2.1 Create RDS PostgreSQL Instance

1. Go to **RDS Console** → Create database
2. Choose:
   - **Engine**: PostgreSQL
   - **Version**: PostgreSQL 15
   - **Template**: Free tier
   - **DB instance identifier**: stockdsl-db
   - **Master username**: postgres
   - **Master password**: (create a strong password)
   - **Instance class**: db.t3.micro (free tier)
   - **Storage**: 20 GB (free tier allows 20GB)
   - **Public access**: No (for security)
   - **VPC**: Default VPC

3. Click "Create database"
4. Wait 5-10 minutes for creation

### 2.2 Configure Security Group

1. Click on your database
2. Go to "Connectivity & security"
3. Click on the security group link
4. Edit inbound rules:
   - Type: PostgreSQL
   - Port: 5432
   - Source: (your Elastic Beanstalk security group - we'll add this later)

### 2.3 Get Connection Details

Note these down (you'll need them):
- **Endpoint**: stockdsl-db.xxxxx.us-east-1.rds.amazonaws.com
- **Port**: 5432
- **Database**: postgres (or create 'stockdsl' database)
- **Username**: postgres
- **Password**: (your password)

### 2.4 Create the stockdsl Database

Connect to RDS and create the database:

```bash
# Install psql if needed
brew install postgresql

# Connect to RDS (from allowed IP)
psql -h stockdsl-db.xxxxx.us-east-1.rds.amazonaws.com -U postgres

# Create database
CREATE DATABASE stockdsl;
\q
```

---

## Step 3: Deploy Backend (Elastic Beanstalk)

### 3.1 Build the Docker Image

```bash
cd fullstack-app/backend

# Build the JAR file
./mvnw clean package -DskipTests

# Build Docker image
docker build -t stockdsl-backend .

# Test locally
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/stockdsl \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=testSecretKey123456789 \
  stockdsl-backend
```

### 3.2 Push to ECR (Elastic Container Registry)

```bash
# Create ECR repository
aws ecr create-repository --repository-name stockdsl-backend

# Get login command
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag stockdsl-backend:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/stockdsl-backend:latest

# Push to ECR
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/stockdsl-backend:latest
```

### 3.3 Create Elastic Beanstalk Application

1. Go to **Elastic Beanstalk Console**
2. Click "Create application"
3. Configure:
   - **Application name**: stockdsl
   - **Platform**: Docker
   - **Platform branch**: Docker running on 64bit Amazon Linux 2
   - **Application code**: Upload your code (or use ECR image)

### 3.4 Create Dockerrun.aws.json

Create this file in your backend folder:

```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/stockdsl-backend:latest",
    "Update": "true"
  },
  "Ports": [
    {
      "ContainerPort": 8080,
      "HostPort": 8080
    }
  ]
}
```

### 3.5 Configure Environment Variables

In Elastic Beanstalk Console:
1. Go to your environment
2. Configuration → Software → Edit
3. Add environment variables:

```
DATABASE_URL = jdbc:postgresql://stockdsl-db.xxxxx.rds.amazonaws.com:5432/stockdsl
DATABASE_USERNAME = postgres
DATABASE_PASSWORD = your-password
JWT_SECRET = your-long-random-secret-key-here
CORS_ORIGINS = https://yourdomain.com
LOG_LEVEL = WARN
```

### 3.6 Configure Security

1. Go to EC2 Console → Security Groups
2. Find the Elastic Beanstalk security group
3. Copy its ID
4. Go back to RDS security group
5. Add inbound rule: PostgreSQL, port 5432, source = EB security group ID

---

## Step 4: Deploy Frontend (S3 + CloudFront)

### 4.1 Build React for Production

```bash
cd fullstack-app/frontend

# Set the API URL for production
echo "REACT_APP_API_URL=https://api.yourdomain.com/api" > .env.production

# Build
npm run build
```

### 4.2 Create S3 Bucket

```bash
# Create bucket (name must be globally unique)
aws s3 mb s3://stockdsl-frontend-YOUR_UNIQUE_ID

# Enable static website hosting
aws s3 website s3://stockdsl-frontend-YOUR_UNIQUE_ID --index-document index.html --error-document index.html
```

### 4.3 Configure Bucket Policy

Create `bucket-policy.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::stockdsl-frontend-YOUR_UNIQUE_ID/*"
    }
  ]
}
```

Apply it:
```bash
aws s3api put-bucket-policy --bucket stockdsl-frontend-YOUR_UNIQUE_ID --policy file://bucket-policy.json
```

### 4.4 Upload Build Files

```bash
# Upload all files
aws s3 sync build/ s3://stockdsl-frontend-YOUR_UNIQUE_ID --delete

# Set proper content types
aws s3 cp s3://stockdsl-frontend-YOUR_UNIQUE_ID s3://stockdsl-frontend-YOUR_UNIQUE_ID --recursive --metadata-directive REPLACE --content-type "text/html" --exclude "*" --include "*.html"
```

### 4.5 Create CloudFront Distribution

1. Go to **CloudFront Console**
2. Create distribution:
   - **Origin domain**: stockdsl-frontend-YOUR_UNIQUE_ID.s3.amazonaws.com
   - **Origin path**: (leave empty)
   - **Viewer protocol policy**: Redirect HTTP to HTTPS
   - **Allowed HTTP methods**: GET, HEAD
   - **Cache policy**: CachingOptimized
   - **Default root object**: index.html

3. Configure error pages (for React Router):
   - Error pages → Create custom error response
   - HTTP error code: 403
   - Response page path: /index.html
   - HTTP response code: 200
   - (Repeat for 404)

4. Note your CloudFront domain: d1234567890.cloudfront.net

---

## Step 5: Set Up Domain (Route 53)

### 5.1 Register Domain (or transfer existing)

1. Go to **Route 53 Console**
2. Registered domains → Register domain
3. Choose a domain (~$12/year for .com)
4. Complete registration

### 5.2 Create Hosted Zone

1. Route 53 → Hosted zones → Create hosted zone
2. Domain name: yourdomain.com
3. Type: Public hosted zone

### 5.3 Request SSL Certificate (ACM)

1. Go to **ACM Console** (Certificate Manager)
2. Request certificate:
   - Domain: yourdomain.com
   - Additional: *.yourdomain.com (wildcard)
   - Validation: DNS validation
3. Click "Create records in Route 53"
4. Wait for validation (5-30 minutes)

### 5.4 Configure DNS Records

In Route 53 hosted zone, create:

**For frontend (yourdomain.com):**
- Type: A
- Alias: Yes
- Route to: CloudFront distribution

**For API (api.yourdomain.com):**
- Type: A
- Alias: Yes
- Route to: Elastic Beanstalk environment

### 5.5 Update CloudFront with Custom Domain

1. Go to CloudFront distribution
2. Edit settings:
   - Alternate domain names: yourdomain.com, www.yourdomain.com
   - Custom SSL certificate: Select your ACM certificate
3. Save changes

### 5.6 Update CORS in Backend

Set environment variable in Elastic Beanstalk:
```
CORS_ORIGINS = https://yourdomain.com,https://www.yourdomain.com
```

---

## Cost Optimization

### Free Tier Limits (first 12 months)

| Service | Free Tier |
|---------|-----------|
| EC2 | 750 hours/month t2.micro |
| RDS | 750 hours/month db.t3.micro, 20GB storage |
| S3 | 5GB storage, 20,000 GET requests |
| CloudFront | 1TB data transfer, 10M requests |
| Data Transfer | 100GB out |

### Tips to Reduce Costs

1. **Stop resources when not using**
   ```bash
   # Stop RDS (no charges when stopped)
   aws rds stop-db-instance --db-instance-identifier stockdsl-db
   ```

2. **Use smallest instance sizes**
   - t3.micro or t2.micro for EB
   - db.t3.micro for RDS

3. **Set up billing alerts**
   - Budget alerts at $10, $20, $50

4. **Delete unused resources**
   - Elastic IPs
   - Old snapshots
   - Unused load balancers

### Estimated Monthly Cost (after free tier)

| Service | Cost |
|---------|------|
| RDS db.t3.micro | ~$15 |
| EC2 t3.micro | ~$8 |
| S3 + CloudFront | ~$5 |
| Route 53 | ~$0.50 |
| **Total** | **~$30/month** |

---

## Troubleshooting

### Backend won't start

1. Check Elastic Beanstalk logs:
   ```bash
   eb logs
   # Or in console: Logs → Request logs
   ```

2. Common issues:
   - Database connection refused → Check security groups
   - Port already in use → Make sure PORT=8080
   - Out of memory → Increase instance size

### Database connection issues

1. Verify security group allows traffic from EB
2. Check RDS endpoint is correct
3. Ensure database 'stockdsl' exists
4. Test connection locally:
   ```bash
   psql -h your-rds-endpoint -U postgres -d stockdsl
   ```

### Frontend shows blank page

1. Check browser console for errors
2. Verify REACT_APP_API_URL is correct
3. Check CloudFront error pages are configured
4. Clear CloudFront cache:
   ```bash
   aws cloudfront create-invalidation --distribution-id YOUR_DIST_ID --paths "/*"
   ```

### CORS errors

1. Check CORS_ORIGINS environment variable
2. Verify it includes https:// (not http://)
3. Check browser network tab for actual origin being sent

---

## Quick Reference Commands

```bash
# Deploy backend
cd backend
./mvnw clean package -DskipTests
eb deploy

# Deploy frontend
cd frontend
npm run build
aws s3 sync build/ s3://your-bucket --delete
aws cloudfront create-invalidation --distribution-id YOUR_ID --paths "/*"

# View logs
eb logs
aws logs tail /aws/elasticbeanstalk/stockdsl/...

# SSH into EC2 instance
eb ssh

# Check RDS status
aws rds describe-db-instances --db-instance-identifier stockdsl-db
```

---

## Learning Resources

### AWS Official
- [AWS Free Tier](https://aws.amazon.com/free/)
- [AWS Getting Started](https://aws.amazon.com/getting-started/)
- [Elastic Beanstalk Docs](https://docs.aws.amazon.com/elasticbeanstalk/)

### Video Tutorials
- [freeCodeCamp AWS Tutorial](https://www.youtube.com/watch?v=3hLmDS179YE) (5 hours)
- [TechWorld with Nana](https://www.youtube.com/c/TechWorldwithNana) (DevOps/AWS)

### Practice
- [AWS Skill Builder](https://skillbuilder.aws/) (free courses)
- [A Cloud Guru](https://acloudguru.com/) (paid, excellent)

---

## Next Steps

After deploying:

1. [ ] Set up CI/CD with GitHub Actions
2. [ ] Add monitoring with CloudWatch
3. [ ] Set up automatic backups for RDS
4. [ ] Configure auto-scaling for traffic spikes
5. [ ] Add WAF (Web Application Firewall) for security
