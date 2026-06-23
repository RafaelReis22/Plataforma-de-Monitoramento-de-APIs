package com.monitoring.agent.metrics;

import com.monitoring.agent.collector.CpuMetricsCollector;
import com.monitoring.agent.collector.DiskMetricsCollector;
import com.monitoring.agent.collector.MemoryMetricsCollector;
import com.monitoring.agent.collector.NetworkMetricsCollector;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareMetricsService {

    private final MeterRegistry meterRegistry;
    private final CpuMetricsCollector cpuCollector;
    private final MemoryMetricsCollector memoryCollector;
    private final NetworkMetricsCollector networkCollector;
    private final DiskMetricsCollector diskCollector;

    private final AtomicReference<MetricSnapshot> latestSnapshot =
        new AtomicReference<>(MetricSnapshot.empty());

    @PostConstruct
    public void registerGauges() {
        Gauge.builder("monitoring.cpu.usage.percent",
                latestSnapshot, s -> s.get().cpuUsagePercent())
            .description("CPU usage percentage")
            .baseUnit("percent")
            .register(meterRegistry);

        Gauge.builder("monitoring.memory.used.bytes",
                latestSnapshot, s -> s.get().memoryUsedBytes())
            .description("Used memory in bytes")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.memory.total.bytes",
                latestSnapshot, s -> s.get().memoryTotalBytes())
            .description("Total memory in bytes")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.memory.used.percent",
                latestSnapshot, s -> s.get().memoryUsedPercent())
            .description("Memory used percentage")
            .baseUnit("percent")
            .register(meterRegistry);

        Gauge.builder("monitoring.network.bytes.received",
                latestSnapshot, s -> s.get().networkBytesReceived())
            .description("Total bytes received via network")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.network.bytes.sent",
                latestSnapshot, s -> s.get().networkBytesSent())
            .description("Total bytes sent via network")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.disk.read.bytes",
                latestSnapshot, s -> s.get().diskReadBytes())
            .description("Total disk read bytes")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.disk.write.bytes",
                latestSnapshot, s -> s.get().diskWriteBytes())
            .description("Total disk write bytes")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.disk.used.bytes",
                latestSnapshot, s -> s.get().diskUsedBytes())
            .description("Used disk space in bytes")
            .baseUnit("bytes")
            .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${monitoring.collection.interval-ms:15000}")
    public void collectMetrics() {
        MetricSnapshot snapshot = new MetricSnapshot(
            Instant.now(),
            cpuCollector.getCpuUsagePercent(),
            memoryCollector.getUsedMemoryBytes(),
            memoryCollector.getTotalMemoryBytes(),
            memoryCollector.getUsedMemoryPercent(),
            networkCollector.getTotalBytesReceived(),
            networkCollector.getTotalBytesSent(),
            diskCollector.getTotalReadBytes(),
            diskCollector.getTotalWriteBytes(),
            diskCollector.getTotalDiskSpaceBytes(),
            diskCollector.getUsedDiskSpaceBytes()
        );
        latestSnapshot.set(snapshot);
        log.debug("Snapshot coletado: CPU={}% RAM={}MB Net_rx={}KB Disk_r={}MB",
            String.format("%.1f", snapshot.cpuUsagePercent()),
            snapshot.memoryUsedBytes() / 1_048_576,
            snapshot.networkBytesReceived() / 1024,
            snapshot.diskReadBytes() / 1_048_576);
    }

    public MetricSnapshot getLatestSnapshot() {
        return latestSnapshot.get();
    }
}
