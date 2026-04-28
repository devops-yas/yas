package com.yas.commonlibrary;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false) // TẮT REUSE ĐỂ TRÁNH RÁC TRÊN JENKINS
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(2));
    }

    @Bean(destroyMethod = "stop")
    public KeycloakContainer keycloakContainer() {
        return new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFiles("/test-realm.json")
            .withReuse(false) // TẮT REUSE
            .withStartupTimeout(Duration.ofMinutes(5));
    }

    @Bean
    public DynamicPropertyRegistrar integrationTestProperties(
            PostgreSQLContainer<?> postgresContainer, 
            KeycloakContainer keycloakContainer) {
        return registry -> {
            // DATABASE PROPERTIES - CHỐT HẠ LỖI DIALECT
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
            registry.add("spring.datasource.username", postgresContainer::getUsername);
            registry.add("spring.datasource.password", postgresContainer::getPassword);
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
            registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

            // KEYCLOAK PROPERTIES
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/quarkus");
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/quarkus/protocol/openid-connect/certs");
        };
    }
}