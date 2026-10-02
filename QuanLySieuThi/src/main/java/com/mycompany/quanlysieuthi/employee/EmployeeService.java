package com.mycompany.quanlysieuthi.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

public final class JpaUtil {

    private static final String PERSISTENCE_UNIT_NAME = "SupermarketPU";
    private static final EntityManagerFactory EMF;

    static {
        Map<String, Object> props = DatabaseConfig.buildProperties();
        EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
    }

    private JpaUtil() {}

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void close() {
        if (EMF.isOpen()) EMF.close();
    }
}