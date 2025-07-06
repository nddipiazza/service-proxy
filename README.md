# Service Proxy Server

A WireMock-based HTTP proxy server that routes requests to different backend services based on URL paths.

## Features

- **Path-based Routing**: Routes `/am/*` and `/services/*` to configurable backend URLs
- **Full HTTP Support**: Supports all HTTP methods (GET, POST, PUT, DELETE, etc.)
- **Request Matching**: Built on WireMock for advanced request matching capabilities
- **Configurable**: Easy configuration via properties files or system properties
- **Health Checks**: Built-in health check endpoint
- **Logging**: Comprehensive logging with configurable levels
- **Testing**: Unit tests included

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+ 

### Build and Run (HTTP)

1. **Clone and build the project:**
   ```bash
   mvn clean compile
   ```

2. **Run the server:**
   ```bash
   mvn exec:java
   ```

3. **Or build an executable JAR:**
   ```bash
   mvn clean package
   java -jar target/service-proxy-1.0.0.jar
   ```

The server will start on port 9095 by default.

### Build and Run with HTTPS

1. **Generate self-signed certificate and run with HTTPS:**
   ```bash
   ./start-https.sh
   ```

2. **Or manually:**
   ```bash
   # Generate certificate
   ./generate-cert.sh
   
   # Build and run with HTTPS
   mvn clean package
   java -Dhttps.enabled=true -Dkeystore.path=certs/keystore.p12 -Dkeystore.password=changeit -Dkeystore.key.password=changeit -jar target/service-proxy-1.0.0.jar
   ```

The HTTPS server will start on port 9443 by default.

### VS Code Tasks

You can use the following VS Code tasks:
- **Build Service Proxy**: Builds the project
- **Run Service Proxy**: Runs with HTTP 
- **Run Service Proxy with HTTPS**: Builds, generates certificate, and runs with HTTPS
- **Generate Self-Signed Certificate**: Creates SSL certificate
- **Test Service Proxy**: Runs unit tests
- **Clean**: Cleans build artifacts

## Configuration

### Default Configuration

- **Server Port**: 9095 (HTTP)
- **HTTPS Port**: 9443 (when HTTPS is enabled)
- **AM Target**: http://localhost:9001
- **Services Target**: http://localhost:9002

### Customizing Configuration

#### Via System Properties (HTTP)
```bash
java -Dserver.port=9090 -Dam.target.url=https://am.example.com -Dservices.target.url=https://services.example.com -jar target/service-proxy-1.0.0.jar
```

#### Via System Properties (HTTPS)
```bash
java -Dhttps.enabled=true -Dhttps.port=8443 -Dkeystore.path=certs/keystore.jks -Dkeystore.password=changeit -Dam.target.url=https://am.example.com -Dservices.target.url=https://services.example.com -jar target/service-proxy-1.0.0.jar
```

#### Via Environment Variables
```bash
export server.port=9090
export am.target.url=https://am.example.com
export services.target.url=https://services.example.com
java -jar target/service-proxy-1.0.0.jar
```

#### Via application.properties
Edit `src/main/resources/application.properties`:
```properties
server.port=9090
am.target.url=https://am.example.com
services.target.url=https://services.example.com
```

## Usage

### Proxy Endpoints

Once running, the following proxy behavior is available:

- **AM Service**: `http://localhost:8080/am/*` → Routes to AM target URL
- **Services**: `http://localhost:8080/services/*` → Routes to Services target URL

### Built-in Endpoints

- **Health Check**: `GET http://localhost:8080/health`
- **Server Info**: `GET http://localhost:8080/`

### Example Requests

#### HTTP Mode
```bash
# Health check
curl http://localhost:9095/health

# Server information
curl http://localhost:9095/

# Proxy to AM service
curl http://localhost:9095/am/users/123

# Proxy to Services
curl -X POST http://localhost:9095/services/data \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

#### HTTPS Mode
```bash
# Health check (use -k to ignore self-signed certificate warnings)
curl -k https://localhost:9443/health

# Server information
curl -k https://localhost:9443/

# Proxy to AM service
curl -k https://localhost:9443/am/users/123

# Proxy to Services
curl -k -X POST https://localhost:9443/services/data \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

## Advanced Usage

### Adding Custom Request Matching

The server is built on WireMock, so you can add sophisticated request matching rules:

```java
// Example: Add custom behavior for specific requests
ServiceProxyServer server = new ServiceProxyServer();
server.start();

// Add custom mapping programmatically
server.addCustomMapping("/am/special/.*", "http://special.example.com", "GET");
```

### WireMock Integration

Since this is built on WireMock, you can leverage all of WireMock's features:

- Request matching by headers, body content, query parameters
- Response transformation and templating
- Request/response recording and playback
- Fault injection for testing

## Development

### Project Structure

```
src/
├── main/java/com/example/serviceproxy/
│   ├── ServiceProxyServer.java          # Main application
│   └── config/
│       └── ProxyConfig.java             # Configuration utilities
├── main/resources/
│   ├── application.properties           # Default configuration
│   └── logback.xml                      # Logging configuration
└── test/java/com/example/serviceproxy/
    └── ServiceProxyServerTest.java      # Unit tests
```

### Running Tests

```bash
mvn test
```

### Building

```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package JAR
mvn package

# Clean and rebuild
mvn clean package
```

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 9095 | Port for the HTTP proxy server |
| `https.enabled` | false | Enable HTTPS mode |
| `https.port` | 9443 | Port for the HTTPS proxy server |
| `keystore.path` | certs/keystore.p12 | Path to the SSL keystore file |
| `keystore.password` | changeit | Password for the SSL keystore |
| `am.target.url` | http://localhost:9001 | Target URL for /am/* requests |
| `services.target.url` | http://localhost:9002 | Target URL for /services/* requests |
| `logging.enabled` | true | Enable/disable logging |
| `logging.level` | INFO | Logging level (DEBUG, INFO, WARN, ERROR) |

## Logging

Logs are written to:
- Console (STDOUT)
- File: `logs/service-proxy.log` (rotated daily)

Log level can be configured via `logging.level` property.

## License

This project is provided as-is for demonstration purposes.

## Contributing

Feel free to submit issues and enhancement requests!
