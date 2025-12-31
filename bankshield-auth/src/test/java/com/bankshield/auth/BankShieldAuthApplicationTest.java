package com.bankshield.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.root=OFF",
    "logging.level.org.springframework.boot=OFF"
})
class BankShieldAuthApplicationTest {
    
    @Test
    void contextLoads() {
        // Test that the Spring Boot application context loads successfully
    }
}