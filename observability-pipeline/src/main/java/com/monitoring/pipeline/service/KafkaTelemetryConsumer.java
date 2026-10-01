package com.monitoring.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("kafka")
public class KafkaTelemetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTelemetryConsumer.class);
    private final PipelineService pipelineService;

    public KafkaTelemetryConsumer(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void processKafkaEvent(Map<String, Object> event) {
        log.info("[KafkaConsumer] Processando evento recebido do tópico Kafka: {}", event);
        try {
            String key = event.getOrDefault("id", System.currentTimeMillis()).toString();
            log.info("[KafkaConsumer] Evento processado com sucesso para chave '{}'", key);
        } catch (Exception e) {
            log.error("[KafkaConsumer] Erro ao processar evento Kafka", e);
        }
    }
}
