package com.monitoring.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ObservabilityPipelineApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityPipelineApplication.class, args);
    }
}
