package com.yas.commonlibrary;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16")
            .withReuse(false)
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    }

    @Bean(destroyMethod = "stop")
    public KeycloakContainer keycloakContainer() {
        // Nên chỉ định rõ image version tương thích với code của bạn (ví dụ 26.0)
        return new KeycloakContainer("quay.io/keycloak/keycloak:26.0") 
            .withRealmImportFiles("/test-realm.json")
            .withReuse(false)
            // Thêm lệnh này để bỏ qua các cấu hình Production phức tạp khi test
            .withExtraArgs("start-dev") 
            // Tăng thời gian chờ khởi động vì Keycloak startup khá nặng
            .withStartupTimeout(java.time.Duration.ofMinutes(3));
    }

    @Bean
    public DynamicPropertyRegistrar databaseProperties() {
        return registry -> {
            // Ép cấu hình Dialect trực tiếp bằng code
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        };
    }

    @Bean
    public DynamicPropertyRegistrar keycloakDynamicProperties(KeycloakContainer keycloakContainer) {
        return registry -> {
            registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/quarkus"
            );
            registry.add(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl()
                    + "/realms/quarkus/protocol/openid-connect/certs"
            );
        };
    }
}