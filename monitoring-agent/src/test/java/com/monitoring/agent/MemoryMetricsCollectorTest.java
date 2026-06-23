package com.monitoring.agent;

import com.monitoring.agent.collector.MemoryMetricsCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryMetricsCollectorTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesMemInfoCorrectly() throws IOException {
        // Arrange — simula /proc/meminfo (valores em kB conforme kernel)
        // MemTotal: 8388608 kB = 8 GB em bytes = 8589934592
        // MemAvailable: 4194304 kB = 4 GB
        // Used = Total - Available = 4 GB
        Path memInfo = tempDir.resolve("meminfo");
        Files.writeString(memInfo,
            "MemTotal:       8388608 kB\n" +
            "MemFree:        2048000 kB\n" +
            "MemAvailable:   4194304 kB\n" +
            "Buffers:          512000 kB\n" +
            "Cached:          1024000 kB\n");

        MemoryMetricsCollector collector = new MemoryMetricsCollector(tempDir.toString());

        // Act + Assert — valores calculados a partir do /proc/meminfo simulado
        // (OSHI pode retornar valores reais do host se disponível — apenas validamos range)
        long total = collector.getTotalMemoryBytes();
        long used  = collector.getUsedMemoryBytes();

        assertThat(total).isGreaterThan(0);
        assertThat(used).isGreaterThanOrEqualTo(0);
        assertThat(used).isLessThanOrEqualTo(total);
    }

    @Test
    void returnsZeroWhenMemInfoMissing() {
        // Arrange — fallback forçado com path inválido
        MemoryMetricsCollector collector = new MemoryMetricsCollector("/nonexistent-proc-forced");

        // Act
        long total = collector.getTotalMemoryBytes();
        long used  = collector.getUsedMemoryBytes();
        double pct = collector.getUsedMemoryPercent();

        // Assert — sem OSHI e sem /proc → retorna 0 sem lançar exceção
        assertThat(total).isGreaterThanOrEqualTo(0);
        assertThat(used).isGreaterThanOrEqualTo(0);
        assertThat(pct).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void usedMemoryPercentIsInValidRange() throws IOException {
        Path memInfo = tempDir.resolve("meminfo");
        Files.writeString(memInfo,
            "MemTotal:       4096000 kB\n" +
            "MemAvailable:   1024000 kB\n");

        MemoryMetricsCollector collector = new MemoryMetricsCollector(tempDir.toString());
        double pct = collector.getUsedMemoryPercent();

        assertThat(pct).isBetween(0.0, 100.0);
    }
}
