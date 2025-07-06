package com.example.serviceproxy.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration management for the Service Proxy Server
 */
public class ProxyConfig {
    
    private static final Properties properties = new Properties();
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try (InputStream input = ProxyConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Properties file not found, will use defaults and system properties
        }
    }
    
    public static String getProperty(String key, String defaultValue) {
        // System properties take precedence
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        
        return properties.getProperty(key, defaultValue);
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    // Configuration getters
    public static int getServerPort() {
        return getIntProperty("server.port", 8080);
    }
    
    public static String getAmTargetUrl() {
        return getProperty("am.target.url", "http://localhost:9001");
    }
    
    public static String getServicesTargetUrl() {
        return getProperty("services.target.url", "http://localhost:9002");
    }
    
    public static boolean isLoggingEnabled() {
        return getBooleanProperty("logging.enabled", true);
    }
    
    public static String getLogLevel() {
        return getProperty("logging.level", "INFO");
    }
}
