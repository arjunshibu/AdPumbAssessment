#!/bin/bash

# Create output directory if it doesn't exist
mkdir -p out

# Clean previous build
rm -rf out/*

# Create manifest files
echo "Main-Class: com.adpumb.proxy.core.OffshoreProxyServer" > out/server_manifest.txt
echo "Main-Class: com.adpumb.proxy.core.ShipProxyClient" > out/client_manifest.txt

# Compile all Java files
if ! javac -sourcepath src -d out \
    src/com/adpumb/proxy/core/*.java \
    src/com/adpumb/proxy/config/*.java \
    src/com/adpumb/proxy/command/*.java \
    src/com/adpumb/proxy/event/*.java \
    src/com/adpumb/proxy/handler/*.java \
    src/com/adpumb/proxy/handler/strategy/*.java; then
    echo "Compilation failed. Please fix the errors and try again."
    exit 1
fi

# Create JAR files only if compilation succeeded
# Create server JAR
jar cfm out/OffshoreProxyServer.jar out/server_manifest.txt -C out com/adpumb/proxy/core/OffshoreProxyServer.class \
    -C out com/adpumb/proxy/core/AbstractProxyComponent.class \
    -C out com/adpumb/proxy/core/ProxyComponent.class \
    -C out com/adpumb/proxy/config \
    -C out com/adpumb/proxy/command \
    -C out com/adpumb/proxy/event \
    -C out com/adpumb/proxy/handler \
    -C out com/adpumb/proxy/handler/strategy \
    -C src application.properties

# Create client JAR
jar cfm out/ShipProxyClient.jar out/client_manifest.txt -C out com/adpumb/proxy/core/ShipProxyClient.class \
    -C out com/adpumb/proxy/core/AbstractProxyComponent.class \
    -C out com/adpumb/proxy/core/ProxyComponent.class \
    -C out com/adpumb/proxy/config \
    -C out com/adpumb/proxy/command \
    -C out com/adpumb/proxy/event \
    -C out com/adpumb/proxy/handler \
    -C out com/adpumb/proxy/handler/strategy \
    -C src application.properties

echo "Build complete! JAR files are in the out directory." 
