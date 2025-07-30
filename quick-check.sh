#!/bin/bash

# =============================================================================
# Quick Status Check
# =============================================================================

SERVER_HOST="14.225.192.15"
SERVER_USER="root"
SERVER_PORT="8085"
DEPLOY_DIR="/var/www/be"

echo "🔍 Quick status check..."
echo "========================"

# Check if application is running
echo -n "Application status: "
if ssh ${SERVER_USER}@${SERVER_HOST} "lsof -i:${SERVER_PORT}" > /dev/null 2>&1; then
    echo "✅ RUNNING"
    
    # Get PID
    PID=$(ssh ${SERVER_USER}@${SERVER_HOST} "lsof -t -i:${SERVER_PORT}")
    echo "PID: $PID"
    
    # Show recent logs
    echo ""
    echo "📋 Recent logs (last 5 lines):"
    ssh ${SERVER_USER}@${SERVER_HOST} "tail -5 ${DEPLOY_DIR}/app.log 2>/dev/null || echo 'No logs available'"
    
else
    echo "❌ NOT RUNNING"
    
    # Check if jar file exists
    echo ""
    echo -n "JAR file: "
    if ssh ${SERVER_USER}@${SERVER_HOST} "test -f ${DEPLOY_DIR}/be.jar"; then
        echo "✅ EXISTS"
    else
        echo "❌ MISSING"
    fi
    
    # Show error logs
    echo ""
    echo "📋 Error logs (last 10 lines):"
    ssh ${SERVER_USER}@${SERVER_HOST} "tail -10 ${DEPLOY_DIR}/app.log 2>/dev/null || echo 'No logs available'"
fi

echo ""
echo "🌐 Expected URL: http://${SERVER_HOST}:${SERVER_PORT}"
