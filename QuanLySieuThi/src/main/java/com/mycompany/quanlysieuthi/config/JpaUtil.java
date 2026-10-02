package com.mycompany.quanlysieuthi.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory(
                    "SupermarketPU",
                    DatabaseConfig.buildProperties()
            );

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }
}