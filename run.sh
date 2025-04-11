#!/bin/bash

# Make sure the JARs exist
if [ ! -f "out/OffshoreProxyServer.jar" ] || [ ! -f "out/ShipProxyClient.jar" ]; then
    echo "JAR files not found. Please run build.sh first."
    exit 1
fi

# Function to run the server
run_server() {
    echo "Starting Offshore Proxy Server..."
    java -cp out/OffshoreProxyServer.jar com.adpumb.proxy.core.OffshoreProxyServer
}

# Function to run the client
run_client() {
    echo "Starting Ship Proxy Client..."
    java -cp out/ShipProxyClient.jar com.adpumb.proxy.core.ShipProxyClient
}

# Check command line arguments
if [ "$1" == "server" ]; then
    run_server
elif [ "$1" == "client" ]; then
    run_client
else
    echo "Usage: ./run.sh [server|client]"
    echo "  server - Run the Offshore Proxy Server"
    echo "  client - Run the Ship Proxy Client"
    exit 1
fi
