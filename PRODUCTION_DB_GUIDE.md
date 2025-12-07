# Database Configuration Guide for Production

This guide explains how to securely manage database credentials for your Student Management System across different environments (Local, AWS EC2, AWS EKS, Render).

## ⚠️ CRITICAL SECURITY WARNING
**NEVER** commit secrets (passwords, API keys) to Git.
**NEVER** hardcode passwords in your source code (like `application.yml`).

---

## 1. Local Development (Docker Compose)

For local development, we use a `.env` file. This file is ignored by Git (via `.gitignore`).

### Step 1: Create a `.env` file
Create a file named `.env` in your `backend` directory:

```properties
# .env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=Raji@1234
POSTGRES_DB=student_db
```

### Step 2: Update `docker-compose.yml`
Reference these variables in your `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
```

### Step 3: Update Spring Boot `application.yml`
In your microservices (e.g., `student-service/src/main/resources/application.yml`), use placeholders:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:student_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:password} # Default fallback only for local dev
```

---

## 2. AWS EC2 (Virtual Machine)

When deploying to EC2, you have two main options:

### Option A: Environment Variables (Simplest)
1. SSH into your EC2 instance.
2. Export the variables in your shell profile (`~/.bashrc` or `~/.profile`) or pass them when running the jar/docker container.

**Running Docker on EC2:**
```bash
docker run -d \
  -e DB_HOST=your-rds-endpoint.amazonaws.com \
  -e DB_USER=postgres \
  -e DB_PASSWORD=VerySecurePassword123! \
  -p 8080:8080 \
  my-student-service
```

### Option B: AWS Systems Manager Parameter Store (Best Practice)
1. Go to AWS Console -> Systems Manager -> Parameter Store.
2. Create parameters like `/student-app/prod/db-password` (Type: SecureString).
3. Update your Spring Boot app to fetch these on startup (requires `spring-cloud-starter-aws-parameter-store-config`).

---

## 3. AWS EKS (Kubernetes)

In Kubernetes, we use **Secrets**.

### Step 1: Create a Kubernetes Secret
Run this command on your cluster (or use a YAML file, but don't commit the YAML with the real password):

```bash
kubectl create secret generic db-credentials \
  --from-literal=username=postgres \
  --from-literal=password='VerySecurePassword123!'
```

### Step 2: Use Secret in Deployment YAML
Update your `k8s/student-service.yaml`:

```yaml
env:
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: password
  - name: DB_USER
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: username
```

---

## 4. Render (PaaS)

Render makes this very easy with their dashboard.

1. Go to your **Dashboard** -> Select your Service.
2. Click **Environment**.
3. Add Environment Variables:
   *   `DB_HOST`: (Your Render PostgreSQL internal URL)
   *   `DB_USER`: `postgres`
   *   `DB_PASSWORD`: (The password Render gave you)

---

## Summary Checklist
- [ ] Remove `Raji@1234` from all `application.yml` files.
- [ ] Replace with `${DB_PASSWORD}`.
- [ ] Add `.env` to `.gitignore`.
- [ ] Use Secrets Management for Production.
