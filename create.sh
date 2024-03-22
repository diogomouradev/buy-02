#!/bin/bash

# Check if mkcert is already installed
if ! command -v mkcert &> /dev/null; then
    echo "mkcert is not installed. Installing..."
    
    # Install prerequisites
    sudo apt update
    sudo apt install -y libnss3-tools
    
    # Download mkcert
    wget -O mkcert https://github.com/FiloSottile/mkcert/releases/download/v1.4.4/mkcert-v1.4.4-linux-amd64
    
    # Make it executable
    chmod +x mkcert
    
    # Move it to /usr/local/bin to make it available globally
    sudo mv mkcert /usr/local/bin/
    
    # Install the local CA
    mkcert -install
    
    echo "mkcert has been installed successfully."
else
    echo "mkcert is already installed."
fi

# Define the directory where the certificates will be placed
CERT_DIR="./backend/microservices/api-gateway/src/main/resources"

# Create the directory if it doesn't exist
mkdir -p $CERT_DIR

# Define certificate details
CERT_NAME="localhost"
CERT_FILE="$CERT_DIR/$CERT_NAME.pem"
KEY_FILE="$CERT_DIR/$CERT_NAME-key.pem"
PKCS12_FILE="$CERT_DIR/$CERT_NAME.p12"
PASSWORD="yourpassword" # Change this password

# Generate a certificate and key with mkcert
mkcert -key-file $KEY_FILE -cert-file $CERT_FILE $CERT_NAME

# Install the local CA certificate for automatic trust
mkcert -install

# Export the CA certificate from mkcert
CA_CERT=$(mkcert -CAROOT)/rootCA.pem

# Import CA certificate into Java trust store
$JAVA_HOME/bin/keytool -importcert -noprompt -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -alias mkcert -file $CA_CERT

# Convert PEM to PKCS12 format
openssl pkcs12 -export -out $PKCS12_FILE -inkey $KEY_FILE -in $CERT_FILE -name "localhost" -password pass:$PASSWORD

echo "SSL certificate generated in $CERT_DIR"