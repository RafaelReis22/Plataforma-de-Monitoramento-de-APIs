package com.monitoring.agent.metrics;

import java.time.Instant;

public record MetricSnapshot(
    Instant timestamp,
    double cpuUsagePercent,
    long memoryUsedBytes,
    long memoryTotalBytes,
    double memoryUsedPercent,
    long networkBytesReceived,
    long networkBytesSent,
    long diskReadBytes,
    long diskWriteBytes,
    long diskTotalBytes,
    long diskUsedBytes
) {
    public static MetricSnapshot empty() {
        return new MetricSnapshot(Instant.now(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
