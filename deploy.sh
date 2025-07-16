echo "Building app..."
./mvnw clean package -DskipTests

echo "Deploy files to server..."
scp -r  target/be.jar root@14.225.192.15:/var/www/be/

ssh root@14.225.192.15 <<EOF
# Tạo thư mục nếu chưa có
sudo mkdir -p /var/www/be

# Kill process cũ nếu có
pid=\$(sudo lsof -t -i:8085)
if [ -z "\$pid" ]; then
    echo "Start server..."
else
    echo "Restart server..."
    sudo kill -9 "\$pid"
    sleep 2
fi

# Chuyển đến thư mục và chạy
cd /var/www/be
nohup java -jar be.jar > app.log 2>&1 &
echo "Server started in background"
EOF
exit
echo "Done!"