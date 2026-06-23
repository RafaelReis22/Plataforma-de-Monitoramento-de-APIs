package com.monitoring.agent;

import com.monitoring.agent.metrics.MetricSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MetricSnapshotTest {

    @Test
    void emptySnapshotHasZeroValues() {
        MetricSnapshot snapshot = MetricSnapshot.empty();

        assertThat(snapshot.cpuUsagePercent()).isZero();
        assertThat(snapshot.memoryUsedBytes()).isZero();
        assertThat(snapshot.memoryTotalBytes()).isZero();
        assertThat(snapshot.networkBytesReceived()).isZero();
        assertThat(snapshot.diskReadBytes()).isZero();
        assertThat(snapshot.timestamp()).isNotNull();
    }

    @Test
    void snapshotIsImmutable() {
        Instant now = Instant.now();
        MetricSnapshot snapshot = new MetricSnapshot(
            now, 45.5, 2_147_483_648L, 8_589_934_592L, 25.0,
            1_000_000L, 500_000L, 2_000_000L, 1_000_000L, 100_000_000L, 50_000_000L
        );

        // Records são imutáveis por definição — sem setters
        assertThat(snapshot.cpuUsagePercent()).isEqualTo(45.5);
        assertThat(snapshot.memoryUsedPercent()).isEqualTo(25.0);
        assertThat(snapshot.timestamp()).isEqualTo(now);
    }

    @Test
    void snapshotEquality() {
        Instant ts = Instant.parse("2026-06-23T00:00:00Z");
        MetricSnapshot s1 = new MetricSnapshot(ts, 50.0, 1024L, 2048L, 50.0, 0L, 0L, 0L, 0L, 0L, 0L);
        MetricSnapshot s2 = new MetricSnapshot(ts, 50.0, 1024L, 2048L, 50.0, 0L, 0L, 0L, 0L, 0L, 0L);

        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }
}
