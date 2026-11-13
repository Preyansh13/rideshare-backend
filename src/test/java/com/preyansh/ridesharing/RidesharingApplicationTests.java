package com.preyansh.ridesharing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class RidesharingApplicationTests {

    // Start a real PostgreSQL database for the application context test.
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("rideshare_test")
                    .withUsername("test")
                    .withPassword("test");

    // Supply the container's database connection details to Spring Boot.
    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        // Passing this test confirms that the Spring application context starts.
    }
}
