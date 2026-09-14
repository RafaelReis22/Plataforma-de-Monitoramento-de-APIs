package com.monitoring.pipeline.service;

import com.monitoring.pipeline.model.MetricSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PipelineService pipelineService;
    private final StreamEventPublisher eventPublisher;

    /**
     * Consome eventos do Redis Stream em lotes periódicos a cada 5 segundos.
     */
    @Scheduled(fixedDelay = 5000)
    public void processarFilaEventos() {
        try {
            List<ObjectRecord<String, MetricSnapshot>> registros = redisTemplate.opsForStream()
                    .read(MetricSnapshot.class, org.springframework.data.redis.connection.stream.StreamOffset.fromStart(StreamEventPublisher.STREAM_KEY));

            if (registros == null || registros.isEmpty()) {
                log.debug("Nenhum novo evento no Redis Stream {}", StreamEventPublisher.STREAM_KEY);
                return;
            }

            int processados = 0;
            for (ObjectRecord<String, MetricSnapshot> record : registros) {
                MetricSnapshot snapshot = record.getValue();
                if (snapshot != null && snapshot.nome() != null) {
                    pipelineService.armazenarSnapshot(snapshot.nome(), snapshot);
                    processados++;
                } else {
                    eventPublisher.enviarParaDeadLetterQueue(snapshot, "Payload de snapshot nulo ou malformado");
                }

                // Remove o registro processado do stream
                redisTemplate.opsForStream().delete(StreamEventPublisher.STREAM_KEY, record.getId());
            }

            log.info("Processados {} eventos de telemetria do Redis Stream com sucesso", processados);
        } catch (Exception e) {
            log.error("Erro ao consumir eventos do Redis Stream: {}", e.getMessage(), e);
        }
    }
}
