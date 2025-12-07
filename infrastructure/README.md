# Infrastructure & Deployment Guide

This folder contains the "Infrastructure as Code" (IaC) to deploy your Student Management System to AWS EC2, just like a real enterprise project.

## 1. Repository Structure Strategy
In large companies like Microsoft or Google:
*   **Monorepo (Unified):** Often used for related services (like our `backend/` folder). It simplifies dependency management and atomic commits.
*   **Polyrepo (Separate):** Used when teams are completely independent.
*   **Our Approach:** We use a **Backend Monorepo** and a **Frontend Repo**. This is a standard, professional pattern.

## 2. AWS EC2 Deployment (Terraform)

We use **Terraform** to create the server automatically.

### Prerequisites
1.  Install Terraform.
2.  Install AWS CLI and run `aws configure`.
3.  Create an SSH Key Pair in AWS Console (EC2 -> Key Pairs) named `sms-key`. Download the `.pem` file.

### Steps to Create Infrastructure
1.  Navigate to `backend/infrastructure/terraform`.
2.  Initialize Terraform:
    ```bash
    terraform init
    ```
3.  Preview changes:
    ```bash
    terraform plan -var="key_name=sms-key"
    ```
4.  Apply (Create Server):
    ```bash
    terraform apply -var="key_name=sms-key"
    ```
5.  Terraform will output the `public_ip` of your new server.

## 3. Deploying Code (CI/CD)

Once the server is running, you can deploy your code.

### Manual Deployment (First Time)
1.  Copy `docker-compose.yml` to the server:
    ```bash
    scp -i path/to/sms-key.pem ../../docker-compose.yml ubuntu@<PUBLIC_IP>:/home/ubuntu/app/
    ```
2.  SSH into the server:
    ```bash
    ssh -i path/to/sms-key.pem ubuntu@<PUBLIC_IP>
    ```
3.  Run the app:
    ```bash
    cd /home/ubuntu/app
    # Export DB Password (IMPORTANT: Don't hardcode!)
    export POSTGRES_PASSWORD=YourSecurePassword
    docker-compose up -d
    ```

### Automated Deployment (GitHub Actions)
To automate this in `ci-cd.yml`, you would add a step using `appleboy/ssh-action` to SSH into this server and run `docker-compose pull && docker-compose up -d`.
