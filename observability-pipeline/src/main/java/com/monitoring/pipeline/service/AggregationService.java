package com.monitoring.pipeline.service;

import com.monitoring.pipeline.model.AggregatedMetric;
import com.monitoring.pipeline.model.MetricSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calcula estatísticas (min, max, média, count) para uma janela de snapshots.
 * Sem dependência de Redis — facilita testes unitários.
 */
@Slf4j
@Service
public class AggregationService {

    /**
     * Agrega uma coleção de snapshots por nome de métrica.
     *
     * @param snapshots snapshots brutos da janela
     * @param inicio    início da janela
     * @param fim       fim da janela
     * @return lista de métricas agregadas, uma por nome distinto
     */
    public List<AggregatedMetric> agregar(Collection<MetricSnapshot> snapshots,
                                           Instant inicio,
                                           Instant fim) {
        if (snapshots == null || snapshots.isEmpty()) {
            log.debug("Nenhum snapshot para agregar na janela {}", inicio);
            return List.of();
        }

        Map<String, DoubleSummaryStatistics> estatisticas = snapshots.stream()
                .collect(Collectors.groupingBy(
                        MetricSnapshot::nome,
                        Collectors.summarizingDouble(MetricSnapshot::valor)
                ));

        return estatisticas.entrySet().stream()
                .map(entrada -> {
                    var stats = entrada.getValue();
                    return new AggregatedMetric(
                            entrada.getKey(),
                            stats.getMin(),
                            stats.getMax(),
                            stats.getAverage(),
                            stats.getCount(),
                            inicio,
                            fim
                    );
                })
                .toList();
    }
}
