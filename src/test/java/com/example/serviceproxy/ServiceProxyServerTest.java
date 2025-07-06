package com.example.serviceproxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ServiceProxyServerTest {
    
    private ServiceProxyServer server;
    private HttpClient httpClient;
    private static final int TEST_PORT = 8089;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private Thread serverThread;
    
    @BeforeEach
    void setUp() throws InterruptedException {
        server = new ServiceProxyServer();
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        // Use a CountDownLatch to wait for server startup
        CountDownLatch serverStarted = new CountDownLatch(1);
        
        // Start server in a separate thread
        serverThread = new Thread(() -> {
            try {
                server.startNonBlocking(TEST_PORT);
                serverStarted.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Wait for server to start (max 5 seconds)
        assertTrue(serverStarted.await(5, TimeUnit.SECONDS), 
                "Server failed to start within 5 seconds");
        
        // Give a little extra time for the server to be fully ready
        Thread.sleep(500);
    }
    
    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }
    
    @Test
    void testHealthEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"));
    }
    
    @Test
    void testRootEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/"))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Service Proxy Server"));
        assertTrue(response.body().contains("/am/*"));
        assertTrue(response.body().contains("/services/*"));
    }
    
    @Test
    void testCustomMappingAddition() {
        // Test that we can add custom mappings programmatically
        assertDoesNotThrow(() -> {
            server.addCustomMapping("/custom/.*", "http://localhost:9999", "GET");
        });
    }
}
