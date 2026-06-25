package com.monitoring.pipeline.service;

import com.monitoring.pipeline.model.AggregatedMetric;
import com.monitoring.pipeline.model.MetricSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AggregationServiceTest {

    private AggregationService service;
    private final Instant inicio = Instant.parse("2024-01-01T00:00:00Z");
    private final Instant fim    = Instant.parse("2024-01-01T00:00:15Z");

    @BeforeEach
    void setUp() {
        service = new AggregationService();
    }

    @Test
    @DisplayName("agrega múltiplos snapshots da mesma métrica corretamente")
    void agregar_multiploSnapshots_calculaEstatisticasCorretas() {
        var snapshots = List.of(
                new MetricSnapshot("cpu.uso", 10.0, "%", inicio, Map.of()),
                new MetricSnapshot("cpu.uso", 30.0, "%", inicio, Map.of()),
                new MetricSnapshot("cpu.uso", 20.0, "%", inicio, Map.of())
        );

        List<AggregatedMetric> resultado = service.agregar(snapshots, inicio, fim);

        assertThat(resultado).hasSize(1);
        AggregatedMetric agregado = resultado.get(0);
        assertThat(agregado.nome()).isEqualTo("cpu.uso");
        assertThat(agregado.min()).isEqualTo(10.0);
        assertThat(agregado.max()).isEqualTo(30.0);
        assertThat(agregado.media()).isCloseTo(20.0, within(0.001));
        assertThat(agregado.count()).isEqualTo(3);
        assertThat(agregado.inicioJanela()).isEqualTo(inicio);
        assertThat(agregado.fimJanela()).isEqualTo(fim);
    }

    @Test
    @DisplayName("agrega múltiplas métricas distintas separadamente")
    void agregar_metricasDistintas_geraAgregadosPorNome() {
        var snapshots = List.of(
                new MetricSnapshot("cpu.uso",   50.0, "%",  inicio, Map.of()),
                new MetricSnapshot("ram.uso",   70.0, "%",  inicio, Map.of()),
                new MetricSnapshot("cpu.uso",   60.0, "%",  inicio, Map.of())
        );

        List<AggregatedMetric> resultado = service.agregar(snapshots, inicio, fim);

        assertThat(resultado).hasSize(2);
        var cpuAgregado = resultado.stream()
                .filter(a -> a.nome().equals("cpu.uso"))
                .findFirst().orElseThrow();
        assertThat(cpuAgregado.count()).isEqualTo(2);
        assertThat(cpuAgregado.media()).isCloseTo(55.0, within(0.001));

        var ramAgregado = resultado.stream()
                .filter(a -> a.nome().equals("ram.uso"))
                .findFirst().orElseThrow();
        assertThat(ramAgregado.count()).isEqualTo(1);
        assertThat(ramAgregado.media()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("retorna lista vazia quando não há snapshots")
    void agregar_semSnapshots_retornaListaVazia() {
        List<AggregatedMetric> resultado = service.agregar(List.of(), inicio, fim);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("retorna lista vazia quando snapshots é null")
    void agregar_snapshotsNull_retornaListaVazia() {
        List<AggregatedMetric> resultado = service.agregar(null, inicio, fim);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("snapshot único resulta em min, max e média iguais ao valor")
    void agregar_snapshotUnico_minMaxMediaIguais() {
        var snapshots = List.of(
                new MetricSnapshot("disco.livre", 500.0, "MB", inicio, Map.of())
        );

        List<AggregatedMetric> resultado = service.agregar(snapshots, inicio, fim);

        assertThat(resultado).hasSize(1);
        var agregado = resultado.get(0);
        assertThat(agregado.min()).isEqualTo(500.0);
        assertThat(agregado.max()).isEqualTo(500.0);
        assertThat(agregado.media()).isEqualTo(500.0);
        assertThat(agregado.count()).isEqualTo(1);
    }
}
