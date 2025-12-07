@echo off
echo Starting Student Management System Microservices...
echo ==================================================
echo NOTE: This script requires Maven (mvn) to be installed and in your system PATH.
echo ==================================================

echo Starting Discovery Service...
start "Discovery Service" cmd /k "cd backend\discovery-service && mvn spring-boot:run"
timeout /t 15

echo Starting API Gateway...
start "API Gateway" cmd /k "cd backend\api-gateway && mvn spring-boot:run"
timeout /t 5

echo Starting Student Service...
start "Student Service" cmd /k "cd backend\student-service && mvn spring-boot:run"

echo Starting Course Service...
start "Course Service" cmd /k "cd backend\course-service && mvn spring-boot:run"

echo Starting Enrollment Service...
start "Enrollment Service" cmd /k "cd backend\enrollment-service && mvn spring-boot:run"

echo Starting Payment Service...
start "Payment Service" cmd /k "cd backend\payment-service && mvn spring-boot:run"

echo Starting Notification Service...
start "Notification Service" cmd /k "cd backend\notification-service && mvn spring-boot:run"

echo Starting Library Service...
start "Library Service" cmd /k "cd backend\library-service && mvn spring-boot:run"

echo Starting Infrastructure Service...
start "Infrastructure Service" cmd /k "cd backend\infrastructure-service && mvn spring-boot:run"

echo All services are starting in separate windows.
pause
