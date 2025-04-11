# AdPumb Proxy System Assessment

This system consists of three components:

1. Offshore Proxy Server (Java)
2. Ship Proxy Client (Java)
3. Simple Server (Python, for testing parallel requests)

## Overview

The system implements a persistent TCP connection between the Ship Proxy Client and Offshore Proxy Server:

- The client establishes a single TCP connection to the server during startup
- All client requests are multiplexed over this persistent connection
- Requests and responses are prefixed with their length to maintain message boundaries
- The connection remains open until the client shuts down, reducing connection overhead

## Docker Deployment

### Building and Running with Docker

```bash
docker-compose up
```

This will start three containers:

1. `proxy-server`: The Offshore Proxy Server
2. `proxy-client`: The Ship Proxy Client
3. `simple-server`: The test server

### Accessing Services in Docker

1. The Proxy Client will be available at `http://localhost:8080`
2. The Proxy Server will be listening on port `http://localhost:9090`
3. The Simple Server will be available at `http://localhost:4000`

### Stopping Docker Services

```bash
docker-compose down
```

## Local Development and Testing

### Prerequisites

- Java Development Kit (JDK) 19 or higher
- Python 3.x (for test server)
- Network connectivity between components

### Building the Project

1. Clone the repository:

2. Build the Java components:

```bash
./build.sh
```

This will create two JAR files in the `out` directory:

- `OffshoreProxyServer.jar`
- `ShipProxyClient.jar`

### Running Components Locally

1. Start the Simple Test Server:

```bash
python3 simple_server.py
```

2. Start the Proxy Server:

```bash
./run.sh server
# or
java -jar out/OffshoreProxyServer.jar
```

3. Start the Proxy Client:

```bash
./run.sh client
# or
java -jar out/ShipProxyClient.jar
```

#### Automated Testing

Run the test script to perform automated testing:

```bash
./test_proxy.sh
```

This script will:

- Test basic proxy functionality
- Test parallel request handling
- Test connection to the public httpbin API (https://httpbin.org)
- Verify response times and error handling

The system is configured to work with both the local Simple Server and the public httpbin API for comprehensive testing.

## Troubleshooting

1. If you encounter port conflicts, ensure no other services are using ports 8080, 9090, or 4000
2. For Docker issues, try:
   ```bash
   docker-compose down
   docker system prune
   docker-compose up --build
   ```
3. For local development issues, ensure all prerequisites are installed and paths are correctly set
