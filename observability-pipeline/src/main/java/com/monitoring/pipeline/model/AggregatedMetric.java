package com.monitoring.pipeline.model;

import java.time.Instant;

/**
 * Resultado da agregação de uma janela de 15 segundos.
 * Armazenado no Redis com TTL de 15 minutos.
 */
public record AggregatedMetric(
        String nome,
        double min,
        double max,
        double media,
        long count,
        Instant inicioJanela,
        Instant fimJanela
) {
    public static AggregatedMetric vazia(String nome, Instant inicio, Instant fim) {
        return new AggregatedMetric(nome, 0, 0, 0, 0, inicio, fim);
    }
}
