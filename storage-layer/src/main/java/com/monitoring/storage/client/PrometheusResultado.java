package com.monitoring.storage.client;

import java.util.List;
import java.util.Map;

// DTO imutável que representa o resultado de uma consulta PromQL ao Prometheus
public record PrometheusResultado(
        // Labels da série temporal (ex: __name__, job, instance)
        Map<String, String> labels,
        // Valores no formato [timestamp_unix, valor_string]
        List<double[]> valores
) {
    public static PrometheusResultado de(Map<String, String> labels, List<double[]> valores) {
        return new PrometheusResultado(Map.copyOf(labels), List.copyOf(valores));
    }
}
