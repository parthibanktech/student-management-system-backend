#!/bin/bash
# Detailed Restart Script for Stability on Low-Memory Servers

echo "Stopping all services..."
sudo docker-compose down

echo "Starting Database and Message Broker (Core)..."
sudo docker-compose up -d postgres zookeeper kafka
echo "Waiting 20 seconds for Database to initialize..."
sleep 20

echo "Starting Discovery Service..."
sudo docker-compose up -d discovery-service
echo "Waiting 20 seconds for Discovery to be ready..."
sleep 20

echo "Starting API Gateway and Frontend..."
sudo docker-compose up -d api-gateway frontend nginx
echo "Waiting 20 seconds for Gateway..."
sleep 20

echo "Starting Student Service..."
sudo docker-compose up -d student-service
echo "Waiting 15 seconds..."
sleep 15

echo "Starting Course Service..."
sudo docker-compose up -d course-service
echo "Waiting 15 seconds..."
sleep 15

echo "Starting Enrollment Service..."
sudo docker-compose up -d enrollment-service
echo "Waiting 15 seconds..."
sleep 15

# echo "Starting remaining services..."
# sudo docker-compose up -d
echo "All services started sequentially. Checking status..."
sudo docker ps
