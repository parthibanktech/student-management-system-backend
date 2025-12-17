#!/bin/bash
echo "=========================================="
echo "      DEBUG REPO - STUDENT SYSTEM        "
echo "=========================================="
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="debug_report_${TIMESTAMP}.txt"

echo "Report generated at: $TIMESTAMP" > $LOG_FILE
echo "" >> $LOG_FILE

echo "[1] MEMORY USAGE" >> $LOG_FILE
free -h >> $LOG_FILE
echo "" >> $LOG_FILE

echo "[2] DISK SPACE" >> $LOG_FILE
df -h >> $LOG_FILE
echo "" >> $LOG_FILE

echo "[3] DOCKER CONTAINERS STATUS" >> $LOG_FILE
sudo docker ps -a >> $LOG_FILE
echo "" >> $LOG_FILE

echo "[4] ENROLLMENT SERVICE LOGS (Last 200 lines)" >> $LOG_FILE
sudo docker logs --tail 200 enrollment-service >> $LOG_FILE 2>&1
echo "" >> $LOG_FILE

echo "[5] KAFKA LOGS (Last 200 lines)" >> $LOG_FILE
sudo docker logs --tail 200 kafka >> $LOG_FILE 2>&1
echo "" >> $LOG_FILE

echo "[6] PAYMENT SERVICE LOGS (Last 200 lines)" >> $LOG_FILE
sudo docker logs --tail 200 payment-service >> $LOG_FILE 2>&1
echo "" >> $LOG_FILE

echo "[7] COURSE SERVICE LOGS (Last 200 lines)" >> $LOG_FILE
sudo docker logs --tail 200 course-service >> $LOG_FILE 2>&1
echo "" >> $LOG_FILE

echo "=========================================="
echo "DEBUG REPORT GENERATED: $LOG_FILE"
echo "Please download or view this file to find the root cause."
echo "=========================================="
