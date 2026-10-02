package com.mycompany.quanlysieuthi.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConfig {

    private DatabaseConfig() {}

    public static Map<String, Object> buildProperties() {
        Properties env = new Properties();

        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream(".env")) {

            if (input == null)
                throw new IllegalStateException("Không tìm thấy .env");

            env.load(new InputStreamReader(input, StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new IllegalStateException("Không thể đọc .env", e);
        }

        Map<String, Object> properties = new HashMap<>();

        properties.put("hibernate.hikari.jdbcUrl",
                env.getProperty("DATABASE_URL"));
        properties.put("hibernate.hikari.username",
                env.getProperty("DATABASE_USERNAME"));
        properties.put("hibernate.hikari.password",
                env.getProperty("DATABASE_PASSWORD"));

        return properties;
    }
}