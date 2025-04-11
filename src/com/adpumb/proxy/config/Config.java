package com.adpumb.proxy.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    private static final Logger logger = LoggerManager.getInstance(Config.class.getName());
    private static Config instance;
    private final Properties properties;

    private static final String CLIENT_PORT_KEY = "proxy.client.port";
    private static final String SERVER_PORT_KEY = "proxy.server.port";
    private static final String SERVER_HOST_KEY = "proxy.server.host";

    private static final String DEFAULT_CLIENT_PORT = "8080";
    private static final String DEFAULT_SERVER_PORT = "9090";
    private static final String DEFAULT_SERVER_HOST = "0.0.0.0";

    private static final String SERVER_HOST_ENV_KEY = "SERVER_HOST";
    private static final String LOCAL_HOST_ENV_KEY = "LOCAL_HOST";
    private static final String LOCAL_PORT_ENV_KEY = "LOCAL_PORT";

    private Config() {
        this.properties = loadProperties();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (input == null) {
                    logger.warning("Unable to find application.properties, using default values");
                    return props;
                }
                props.load(input);
            }
        } catch (IOException e) {
            logger.warning("Error loading application.properties: " + e.getMessage());
        }
        return props;
    }

    public int getClientPort() {
        return Integer.parseInt(properties.getProperty(CLIENT_PORT_KEY, DEFAULT_CLIENT_PORT));
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty(SERVER_PORT_KEY, DEFAULT_SERVER_PORT));
    }

    public String getServerHost() {
        String envHost = System.getenv(SERVER_HOST_ENV_KEY);
        if (envHost != null && !envHost.isEmpty()) {
            return envHost;
        }

        return properties.getProperty(SERVER_HOST_KEY, DEFAULT_SERVER_HOST);
    }

    public Integer getLocalPort() {
        String envPort = System.getenv(LOCAL_PORT_ENV_KEY);
        if (envPort == null || envPort.isEmpty()) {
            return null;
        }
        return Integer.parseInt(envPort);
    }

    public String getLocalHost() {
        String envHost = System.getenv(LOCAL_HOST_ENV_KEY);
        if (envHost == null || envHost.isEmpty()) {
            return null;
        }
        return envHost;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
