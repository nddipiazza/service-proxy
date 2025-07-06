#!/bin/bash

# Create certificate directory
mkdir -p certs

# Generate self-signed certificate and keystore
echo "Generating self-signed certificate for Service Proxy Server..."

# Certificate details
KEYSTORE_PATH="certs/keystore.p12"
KEYSTORE_PASSWORD="changeit"
KEY_PASSWORD="changeit"
CERT_ALIAS="service-proxy"
VALIDITY_DAYS=2365

# Remove existing keystores if they exist
rm -f "certs/keystore.jks" "certs/keystore.p12"

# Generate keystore with self-signed certificate (PKCS12 format)
keytool -genkeypair \
    -alias "$CERT_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS \
    -keystore "$KEYSTORE_PATH" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -storetype PKCS12 \
    -dname "CN=localhost, OU=Service Proxy, O=Example Corp, L=City, ST=State, C=US" \
    -ext "SAN=DNS:localhost,IP:127.0.0.1" \
    -noprompt

if [ $? -eq 0 ]; then
    echo "✅ Self-signed certificate generated successfully!"
    echo "   Keystore: $KEYSTORE_PATH"
    echo "   Store Password: $KEYSTORE_PASSWORD"
    echo "   Key Password: $KEY_PASSWORD"
    echo "   Validity: $VALIDITY_DAYS days"
    echo ""
    echo "Certificate details:"
    keytool -list -v -keystore "$KEYSTORE_PATH" -storepass "$KEYSTORE_PASSWORD" -alias "$CERT_ALIAS" | grep -E "(Alias name|Creation date|Valid from|Owner:|Issuer:|Extensions:|DNS:|IP Address:)" | head -10
else
    echo "❌ Failed to generate certificate"
    exit 1
fi
