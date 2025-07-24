#!/bin/bash

# =============================================================================
# Debug Server Status
# =============================================================================

SERVER_HOST="14.225.192.15"
SERVER_USER="root"
SERVER_PORT="8085"
DEPLOY_DIR="/var/www/be"
APP_NAME="be.jar"

echo "🔍 Debugging server status..."
echo "=================================="

echo "1. Checking if deployment directory exists:"
ssh ${SERVER_USER}@${SERVER_HOST} "ls -la ${DEPLOY_DIR}/"

echo ""
echo "2. Checking for application file:"
ssh ${SERVER_USER}@${SERVER_HOST} "ls -la ${DEPLOY_DIR}/${APP_NAME}"

echo ""
echo "3. Checking running Java processes:"
ssh ${SERVER_USER}@${SERVER_HOST} "ps aux | grep java"

echo ""
echo "4. Checking port 8085:"
ssh ${SERVER_USER}@${SERVER_HOST} "netstat -tlnp | grep 8085"

echo ""
echo "5. Checking application logs (last 20 lines):"
ssh ${SERVER_USER}@${SERVER_HOST} "tail -20 ${DEPLOY_DIR}/app.log 2>/dev/null || echo 'No log file found'"

echo ""
echo "6. Checking if PID file exists:"
ssh ${SERVER_USER}@${SERVER_HOST} "cat ${DEPLOY_DIR}/app.pid 2>/dev/null || echo 'No PID file found'"

echo ""
echo "7. Checking system resources:"
ssh ${SERVER_USER}@${SERVER_HOST} "free -h && df -h ${DEPLOY_DIR}"

echo ""
echo "8. Manual start command (if needed):"
echo "ssh ${SERVER_USER}@${SERVER_HOST}"
echo "cd ${DEPLOY_DIR}"
echo "java -jar ${APP_NAME}"
