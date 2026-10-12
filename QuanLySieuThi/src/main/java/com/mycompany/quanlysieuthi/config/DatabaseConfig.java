package com.mycompany.quanlysieuthi.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static Map<String, Object> buildProperties() {
        Properties env = new Properties();

        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream(".env")) {

            if (input != null) {
                env.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }

        String dbUrl = getEnvOrProperty("DATABASE_URL", env);
        String dbUser = getEnvOrProperty("DATABASE_USERNAME", env);
        String dbPass = getEnvOrProperty("DATABASE_PASSWORD", env);

        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalStateException("DATABASE_URL is not configured (check Render environment variables or .env file)");
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hikari.jdbcUrl", dbUrl);
        properties.put("hibernate.hikari.username", dbUser);
        properties.put("hibernate.hikari.password", dbPass);

        return properties;
    }

    private static String getEnvOrProperty(String key, Properties props) {
        String sysEnv = System.getenv(key);
        if (sysEnv != null && !sysEnv.trim().isEmpty()) {
            return sysEnv.trim();
        }
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return props.getProperty(key);
    }
}