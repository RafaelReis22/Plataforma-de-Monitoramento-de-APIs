package com.monitoring.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("kafka")
public class KafkaTelemetryProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTelemetryProducer.class);

    public void publishTelemetryEvent(String topic, String key, Map<String, Object> payload) {
        log.info("[KafkaProducer] Publicando evento no tópico Kafka '{}' com chave '{}': {}", topic, key, payload);
    }
}
