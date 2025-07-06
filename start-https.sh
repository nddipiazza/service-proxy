#!/bin/bash

echo "🔐 Starting Service Proxy Server with HTTPS..."

# Check if certificate exists
if [ ! -f "certs/keystore.p12" ]; then
    echo "⚠️  Certificate not found. Generating self-signed certificate..."
    ./generate-cert.sh
    if [ $? -ne 0 ]; then
        echo "❌ Failed to generate certificate. Exiting."
        exit 1
    fi
fi

# Build the project if JAR doesn't exist
if [ ! -f "target/service-proxy-1.0.0.jar" ]; then
    echo "📦 Building project..."
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo "❌ Build failed. Exiting."
        exit 1
    fi
fi

echo "🚀 Starting HTTPS server on port 9443..."
echo "   Access your server at: https://localhost:9443"
echo "   Health check: https://localhost:9443/health"
echo "   Server info: https://localhost:9443/"
echo ""
echo "⚠️  Note: You may see certificate warnings in your browser since this is a self-signed certificate."
echo ""

# Start the server with HTTPS
java -Dhttps.enabled=true \
     -Dkeystore.path=certs/keystore.p12 \
     -Dkeystore.password=changeit \
     -Dkeystore.key.password=changeit \
     -jar target/service-proxy-1.0.0.jar
