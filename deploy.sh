echo "Building app..."
./mvnw clean package -DskipTests

echo "Deploy files to server..."
scp -r  target/be.jar root@14.225.198.16:/var/www/be/

ssh root@14.225.198.16 <<EOF
pid=$(sudo lsof -t -i:8085)

if [ -z "$pid" ]; then
    echo "Start server..."
else
    echo "Restart server..."
    sudo kill -9 "$pid"
fi
cd /var/www/be
java -jar be.jar
EOF
exit
echo "Done!"