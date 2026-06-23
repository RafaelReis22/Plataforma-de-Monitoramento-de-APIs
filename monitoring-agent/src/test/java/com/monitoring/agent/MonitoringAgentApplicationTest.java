package com.monitoring.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MonitoringAgentApplicationTest {

    @Test
    void contextLoads() {
        // Spring Boot context sobe sem erros
    }
}
