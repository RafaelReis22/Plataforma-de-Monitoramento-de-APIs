package com.monitoring.pipeline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    // M3 — TTL 5min alinhado com política allkeys-lru do Redis
    private static final Duration RAW_METRIC_TTL     = Duration.ofMinutes(5);
    private static final Duration AGGREGATE_METRIC_TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> redisTemplate;

    public void storeRawMetric(String metricKey, Object snapshot) {
        String key = "metric:raw:" + metricKey;
        redisTemplate.opsForValue().set(key, snapshot, RAW_METRIC_TTL);
        log.debug("Métrica raw armazenada: {} (TTL={})", key, RAW_METRIC_TTL);
    }

    public void storeAggregate(String windowKey, Object aggregate) {
        String key = "metric:agg:" + windowKey;
        redisTemplate.opsForValue().set(key, aggregate, AGGREGATE_METRIC_TTL);
        log.debug("Agregado armazenado: {} (TTL={})", key, AGGREGATE_METRIC_TTL);
    }

    public Object getRawMetric(String metricKey) {
        return redisTemplate.opsForValue().get("metric:raw:" + metricKey);
    }

    // Pipeline de agregação — roda a cada 15s
    @Scheduled(fixedDelay = 15_000)
    public void runAggregationCycle() {
        String windowKey = "window:" + Instant.now().getEpochSecond() / 15;
        log.debug("Ciclo de agregação: {}", windowKey);
        // Agregação será implementada na Fase 4 — stub de orquestração
    }

    // M3 — Monitoramento de memória Redis
    @Scheduled(fixedDelay = 60_000)
    public void checkRedisMemory() {
        try {
            var conn = redisTemplate.getConnectionFactory();
            if (conn == null) return;
            Properties info = (Properties) conn.getConnection()
                .serverCommands().info("memory");
            if (info == null) return;
            long used = Long.parseLong(info.getProperty("used_memory", "0"));
            String maxStr = info.getProperty("maxmemory", "0");
            long max = Long.parseLong(maxStr);
            if (max > 0 && (double) used / max > 0.80) {
                log.warn("Redis em {}% da capacidade — allkeys-lru iniciará evictions em breve",
                    (int)((double) used / max * 100));
            }
        } catch (Exception e) {
            log.debug("Não foi possível verificar memória Redis: {}", e.getMessage());
        }
    }
}
