#!/bin/bash

# =============================================================================
# Simple Deployment Script
# =============================================================================

SERVER_HOST="14.225.192.15"
SERVER_USER="root"
SERVER_PORT="8085"
DEPLOY_DIR="/var/www/be"
APP_NAME="be.jar"

echo "🚀 Starting simple deployment..."
echo "=================================="

# Step 1: Build application
echo "Step 1: Building application..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

if [ ! -f "target/${APP_NAME}" ]; then
    echo "❌ Build artifact not found!"
    exit 1
fi

echo "✅ Build successful"

# Step 2: Stop existing application
echo ""
echo "Step 2: Stopping existing application..."
ssh ${SERVER_USER}@${SERVER_HOST} <<'EOF'
# Kill any existing Java process running be.jar
pkill -f "be.jar" || echo "No existing process found"

# Wait a moment
sleep 2

# Check if port is free
if lsof -i:8085 > /dev/null 2>&1; then
    echo "⚠️ Port 8085 still in use, force killing..."
    fuser -k 8085/tcp || true
    sleep 2
fi

echo "✅ Existing application stopped"
EOF

# Step 3: Create deployment directory
echo ""
echo "Step 3: Preparing deployment directory..."
ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${DEPLOY_DIR}"

# Step 4: Upload application
echo ""
echo "Step 4: Uploading application..."
scp target/${APP_NAME} ${SERVER_USER}@${SERVER_HOST}:${DEPLOY_DIR}/

if [ $? -ne 0 ]; then
    echo "❌ Upload failed!"
    exit 1
fi

echo "✅ Upload successful"

# Step 5: Start application
echo ""
echo "Step 5: Starting application..."
ssh ${SERVER_USER}@${SERVER_HOST} <<EOF
cd ${DEPLOY_DIR}

# Start application
echo "Starting application..."
nohup java -jar -Xmx512m -Xms256m ${APP_NAME} > app.log 2>&1 &
echo \$! > app.pid

echo "Application started with PID: \$(cat app.pid)"
EOF

# Step 6: Wait and verify
echo ""
echo "Step 6: Verifying deployment..."
echo "Waiting 10 seconds for application to start..."

for i in {1..10}; do
    echo -n "."
    sleep 1
done
echo ""

# Check if application is running
ssh ${SERVER_USER}@${SERVER_HOST} <<'EOF'
if lsof -i:8085 > /dev/null 2>&1; then
    echo "✅ Application is running on port 8085"
    echo "🌐 Access at: http://14.225.192.15:8085"
else
    echo "❌ Application is not running on port 8085"
    echo "📋 Recent logs:"
    tail -10 /var/www/be/app.log 2>/dev/null || echo "No logs found"
    exit 1
fi
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Deployment completed successfully!"
    echo "🌐 Application URL: http://${SERVER_HOST}:${SERVER_PORT}"
else
    echo ""
    echo "❌ Deployment failed!"
    echo "💡 Run './debug-server.sh' to troubleshoot"
fi
