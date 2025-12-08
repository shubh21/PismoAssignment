package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer(){
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("pismo")
                .withUsername("pismo_user")
                .withPassword("secure_password_123");
    }
}
