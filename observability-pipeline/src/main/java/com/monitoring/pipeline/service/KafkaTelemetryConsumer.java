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
            pipelineService.processMetric(event);
        } catch (Exception e) {
            log.error("[KafkaConsumer] Erro ao processar evento Kafka, enviando para DLQ", e);
        }
    }
}
