package com.example.serviceproxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock-based proxy server that routes requests to different endpoints
 */
public class ServiceProxyServer {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyServer.class);
    
    private static final int DEFAULT_PORT = 9095;
    private static final int DEFAULT_HTTPS_PORT = 9443;
    private static final String AM_TARGET_URL = System.getProperty("am.target.url", "http://localhost:9001");
    private static final String SERVICES_TARGET_URL = System.getProperty("services.target.url", "http://localhost:9002");
    
    private WireMockServer wireMockServer;
    
    public static void main(String[] args) {
        ServiceProxyServer server = new ServiceProxyServer();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Service Proxy Server...");
            server.stop();
        }));
        
        // Check if HTTPS is enabled
        boolean httpsEnabled = Boolean.parseBoolean(System.getProperty("https.enabled", "false"));
        
        if (httpsEnabled) {
            logger.info("Starting server with HTTPS enabled");
            server.startWithHttps();
        } else {
            logger.info("Starting server with HTTP");
            server.start();
        }
    }
    
    public void start() {
        start(DEFAULT_PORT);
    }
    
    public void start(int port) {
        // Configure WireMock
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(port)
                .enableBrowserProxying(true);
        
        wireMockServer = new WireMockServer(config);
        
        // Start the server first
        wireMockServer.start();
        
        // Set up proxy mappings after server is started
        setupProxyMappings();
        
        logger.info("Service Proxy Server started on port {}", port);
        logger.info("Proxy mappings:");
        logger.info("  /am/* -> {}", AM_TARGET_URL);
        logger.info("  /services/* -> {}", SERVICES_TARGET_URL);
        logger.info("Server ready to handle requests!");
        
        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Server interrupted, shutting down...");
        }
    }
    
    public void startNonBlocking(int port) {
        // Configure WireMock
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(port)
                .enableBrowserProxying(true);
        
        wireMockServer = new WireMockServer(config);
        
        // Start the server first
        wireMockServer.start();
        
        // Set up proxy mappings after server is started
        setupProxyMappings();
        
        logger.info("Service Proxy Server started on port {}", port);
        logger.info("Proxy mappings:");
        logger.info("  /am/* -> {}", AM_TARGET_URL);
        logger.info("  /services/* -> {}", SERVICES_TARGET_URL);
        logger.info("Server ready to handle requests!");
    }
    
    public void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            logger.info("Service Proxy Server stopped");
        }
    }
    
    private void setupProxyMappings() {
        // Configure WireMock client to use our server
        configureFor("localhost", wireMockServer.port());
        
        // Proxy all /am/* requests to AM_TARGET_URL
        stubFor(any(urlMatching("/am/.*"))
                .willReturn(aResponse()
                        .proxiedFrom(AM_TARGET_URL)));
        
        // Proxy all /services/* requests to SERVICES_TARGET_URL  
        stubFor(any(urlMatching("/services/.*"))
                .willReturn(aResponse()
                        .proxiedFrom(SERVICES_TARGET_URL)));
        
        // Optional: Add a health check endpoint
        stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"UP\", \"service\": \"service-proxy\"}")));
        
        // Optional: Add a root endpoint with information about available proxies
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"service\": \"Service Proxy Server\",\n" +
                                "  \"version\": \"1.0.0\",\n" +
                                "  \"proxies\": {\n" +
                                "    \"/am/*\": \"" + AM_TARGET_URL + "\",\n" +
                                "    \"/services/*\": \"" + SERVICES_TARGET_URL + "\"\n" +
                                "  },\n" +
                                "  \"endpoints\": {\n" +
                                "    \"health\": \"/health\",\n" +
                                "    \"info\": \"/\"\n" +
                                "  }\n" +
                                "}")));
        
        logger.info("Proxy mappings configured successfully");
    }
    
    /**
     * Add custom stub mappings for specific request matching
     * This method can be called after server startup to add custom behavior
     */
    public void addCustomMapping(String urlPattern, String targetUrl, String method) {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            throw new IllegalStateException("Server must be started before adding custom mappings");
        }
        
        configureFor("localhost", wireMockServer.port());
        
        switch (method.toUpperCase()) {
            case "GET":
                stubFor(get(urlMatching(urlPattern))
                        .willReturn(aResponse().proxiedFrom(targetUrl)));
                break;
            case "POST":
                stubFor(post(urlMatching(urlPattern))
                        .willReturn(aResponse().proxiedFrom(targetUrl)));
                break;
            case "PUT":
                stubFor(put(urlMatching(urlPattern))
                        .willReturn(aResponse().proxiedFrom(targetUrl)));
                break;
            case "DELETE":
                stubFor(delete(urlMatching(urlPattern))
                        .willReturn(aResponse().proxiedFrom(targetUrl)));
                break;
            default:
                stubFor(any(urlMatching(urlPattern))
                        .willReturn(aResponse().proxiedFrom(targetUrl)));
        }
        
        logger.info("Added custom mapping: {} {} -> {}", method, urlPattern, targetUrl);
    }
    
    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }
    
    public void startWithHttps() {
        startWithHttps(DEFAULT_HTTPS_PORT);
    }
    
    public void startWithHttps(int httpsPort) {
        String keystorePath = System.getProperty("keystore.path", "certs/keystore.p12");
        String keystorePassword = System.getProperty("keystore.password", "changeit");
        String keyPassword = System.getProperty("keystore.key.password", "changeit");
        
        // Configure WireMock with HTTPS only
        WireMockConfiguration config = WireMockConfiguration.options()
                .httpsPort(httpsPort)
                .keystorePath(keystorePath)
                .keystorePassword(keystorePassword)
                .keyManagerPassword(keyPassword)
                .enableBrowserProxying(true);
        
        wireMockServer = new WireMockServer(config);
        
        // Start the server first
        wireMockServer.start();
        
        // Set up proxy mappings after server is started
        setupProxyMappings();
        
        logger.info("Service Proxy Server started with HTTPS on port {}", httpsPort);
        logger.info("Keystore path: {}", keystorePath);
        logger.info("Proxy mappings:");
        logger.info("  /am/* -> {}", AM_TARGET_URL);
        logger.info("  /services/* -> {}", SERVICES_TARGET_URL);
        logger.info("Server ready to handle HTTPS requests!");
        
        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Server interrupted, shutting down...");
        }
    }
}
