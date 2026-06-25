package com.monitoring.pipeline.model;

import java.time.Instant;
import java.util.Map;

/**
 * Representa um snapshot bruto de uma métrica coletada pelos agentes.
 * Armazenado no Redis com TTL de 5 minutos (allkeys-lru).
 */
public record MetricSnapshot(
        String nome,
        double valor,
        String unidade,
        Instant timestamp,
        Map<String, String> tags
) {
    public static MetricSnapshot de(String nome, double valor, String unidade) {
        return new MetricSnapshot(nome, valor, unidade, Instant.now(), Map.of());
    }

    public static MetricSnapshot de(String nome, double valor, String unidade, Map<String, String> tags) {
        return new MetricSnapshot(nome, valor, unidade, Instant.now(), tags);
    }
}
