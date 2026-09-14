package com.monitoring.pipeline.service;

import com.monitoring.pipeline.model.MetricSnapshot;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamEventPublisher {

    public static final String STREAM_KEY = "stream:metrics";
    public static final String DLQ_STREAM_KEY = "stream:metrics:dlq";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Envia um evento individual de métrica para o Redis Stream com proteção de CircuitBreaker e RateLimiter.
     */
    @CircuitBreaker(name = "redisStreamPublisher", fallbackMethod = "fallbackPublicarEvento")
    @RateLimiter(name = "redisStreamPublisher")
    public String publicarEvento(MetricSnapshot snapshot) {
        ObjectRecord<String, MetricSnapshot> record = StreamRecords.newRecord()
                .in(STREAM_KEY)
                .ofObject(snapshot);

        RecordId recordId = redisTemplate.opsForStream().add(record);
        log.debug("Evento publicado no Redis Stream {}: id={}", STREAM_KEY, recordId);
        return recordId != null ? recordId.getValue() : "0-0";
    }

    /**
     * Envia um lote (batch) de métricas de forma atômica para alta vazão.
     */
    @CircuitBreaker(name = "redisStreamBatchPublisher", fallbackMethod = "fallbackPublicarLote")
    public int publicarLote(List<MetricSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) return 0;

        int enviados = 0;
        for (MetricSnapshot snapshot : snapshots) {
            publicarEvento(snapshot);
            enviados++;
        }
        log.info("Lote de {} eventos de telemetria publicado no Redis Stream", enviados);
        return enviados;
    }

    /**
     * Encaminha eventos com falha irrecuperável para a Dead Letter Queue (DLQ).
     */
    public void enviarParaDeadLetterQueue(MetricSnapshot snapshot, String motivo) {
        log.warn("Encaminhando evento para DLQ ({}) por falha: {}", DLQ_STREAM_KEY, motivo);
        ObjectRecord<String, MetricSnapshot> record = StreamRecords.newRecord()
                .in(DLQ_STREAM_KEY)
                .ofObject(snapshot);
        redisTemplate.opsForStream().add(record);
    }

    // Fallbacks do Resilience4j
    public String fallbackPublicarEvento(MetricSnapshot snapshot, Throwable t) {
        log.error("CircuitBreaker ativado ao publicar evento de telemetria: {}", t.getMessage());
        enviarParaDeadLetterQueue(snapshot, "CircuitBreaker: " + t.getMessage());
        return "fallback-id";
    }

    public int fallbackPublicarLote(List<MetricSnapshot> snapshots, Throwable t) {
        log.error("CircuitBreaker ativado ao publicar lote de telemetria: {}", t.getMessage());
        if (snapshots != null) {
            snapshots.forEach(s -> enviarParaDeadLetterQueue(s, "Batch Fallback: " + t.getMessage()));
        }
        return 0;
    }
}
