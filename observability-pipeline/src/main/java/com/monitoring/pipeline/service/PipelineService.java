package com.monitoring.pipeline.service;

import com.monitoring.pipeline.model.AggregatedMetric;
import com.monitoring.pipeline.model.MetricSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    // TTL alinhado com política allkeys-lru do Redis
    private static final Duration RAW_METRIC_TTL      = Duration.ofMinutes(5);
    private static final Duration AGGREGATE_METRIC_TTL = Duration.ofMinutes(15);

    private static final String PREFIX_RAW = "metric:raw:";
    private static final String PREFIX_AGG = "metric:agg:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AggregationService aggregationService;

    /** Armazena um snapshot bruto no Redis com TTL de 5 minutos. */
    public void armazenarSnapshot(String chave, MetricSnapshot snapshot) {
        String redisKey = PREFIX_RAW + chave;
        redisTemplate.opsForValue().set(redisKey, snapshot, RAW_METRIC_TTL);
        log.debug("Snapshot armazenado: {} (TTL={})", redisKey, RAW_METRIC_TTL);
    }

    /** Recupera um snapshot bruto do Redis. Retorna null se expirado ou inexistente. */
    public MetricSnapshot obterSnapshot(String chave) {
        Object valor = redisTemplate.opsForValue().get(PREFIX_RAW + chave);
        if (valor instanceof MetricSnapshot snapshot) {
            return snapshot;
        }
        return null;
    }

    /** Recupera uma métrica agregada pelo nome da janela. */
    public AggregatedMetric obterAgregado(String chaveJanela) {
        Object valor = redisTemplate.opsForValue().get(PREFIX_AGG + chaveJanela);
        if (valor instanceof AggregatedMetric agregado) {
            return agregado;
        }
        return null;
    }

    /** Lista todas as métricas agregadas disponíveis no Redis. */
    public List<AggregatedMetric> listarAgregados() {
        Set<String> chaves = redisTemplate.keys(PREFIX_AGG + "*");
        if (chaves == null || chaves.isEmpty()) {
            return List.of();
        }
        return chaves.stream()
                .map(chave -> redisTemplate.opsForValue().get(chave))
                .filter(Objects::nonNull)
                .filter(v -> v instanceof AggregatedMetric)
                .map(v -> (AggregatedMetric) v)
                .collect(Collectors.toList());
    }

    /** Pipeline de agregação — roda a cada 15 segundos. */
    @Scheduled(fixedDelay = 15_000)
    public void executarCicloAgregacao() {
        Instant inicio = Instant.now().minusSeconds(15);
        Instant fim    = Instant.now();
        String chaveJanela = "window:" + fim.getEpochSecond() / 15;

        Set<String> chaves = redisTemplate.keys(PREFIX_RAW + "*");
        if (chaves == null || chaves.isEmpty()) {
            log.debug("Nenhum snapshot disponível para agregação na janela {}", chaveJanela);
            return;
        }

        List<MetricSnapshot> snapshots = chaves.stream()
                .map(chave -> redisTemplate.opsForValue().get(chave))
                .filter(Objects::nonNull)
                .filter(v -> v instanceof MetricSnapshot)
                .map(v -> (MetricSnapshot) v)
                .collect(Collectors.toList());

        List<AggregatedMetric> agregados = aggregationService.agregar(snapshots, inicio, fim);

        agregados.forEach(agregado -> {
            String redisKey = PREFIX_AGG + chaveJanela + ":" + agregado.nome();
            redisTemplate.opsForValue().set(redisKey, agregado, AGGREGATE_METRIC_TTL);
            log.debug("Agregado salvo: {} (min={}, max={}, media={}, count={})",
                    redisKey, agregado.min(), agregado.max(), agregado.media(), agregado.count());
        });

        log.info("Ciclo de agregação concluído: {} snapshots → {} agregados", snapshots.size(), agregados.size());
    }

    /** Monitoramento de memória Redis — alerta quando acima de 80%. */
    @Scheduled(fixedDelay = 60_000)
    public void verificarMemoriaRedis() {
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) return;
            Properties info = (Properties) connectionFactory.getConnection()
                    .serverCommands().info("memory");
            if (info == null) return;
            long usado = Long.parseLong(info.getProperty("used_memory", "0"));
            long maximo = Long.parseLong(info.getProperty("maxmemory", "0"));
            if (maximo > 0 && (double) usado / maximo > 0.80) {
                log.warn("Redis em {}% da capacidade — allkeys-lru iniciará evictions em breve",
                        (int) ((double) usado / maximo * 100));
            }
        } catch (Exception e) {
            log.debug("Não foi possível verificar memória Redis: {}", e.getMessage());
        }
    }
}
