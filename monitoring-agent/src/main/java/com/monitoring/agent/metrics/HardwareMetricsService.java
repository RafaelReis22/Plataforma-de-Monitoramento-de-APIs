package com.monitoring.agent.metrics;

import com.monitoring.agent.collector.CpuMetricsCollector;
import com.monitoring.agent.collector.MemoryMetricsCollector;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareMetricsService {

    private final MeterRegistry meterRegistry;
    private final CpuMetricsCollector cpuCollector;
    private final MemoryMetricsCollector memoryCollector;

    private volatile double currentCpuUsage = 0.0;
    private volatile long currentMemoryUsed = 0L;
    private volatile long currentMemoryTotal = 0L;

    @PostConstruct
    public void registerGauges() {
        Gauge.builder("monitoring.cpu.usage.percent", this, HardwareMetricsService::getCurrentCpuUsage)
            .description("CPU usage percentage")
            .baseUnit("percent")
            .register(meterRegistry);

        Gauge.builder("monitoring.memory.used.bytes", this, HardwareMetricsService::getCurrentMemoryUsed)
            .description("Used memory in bytes")
            .baseUnit("bytes")
            .register(meterRegistry);

        Gauge.builder("monitoring.memory.total.bytes", this, HardwareMetricsService::getCurrentMemoryTotal)
            .description("Total memory in bytes")
            .baseUnit("bytes")
            .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${monitoring.collection.interval-ms:15000}")
    public void collectMetrics() {
        currentCpuUsage = cpuCollector.getCpuUsagePercent();
        currentMemoryUsed = memoryCollector.getUsedMemoryBytes();
        currentMemoryTotal = memoryCollector.getTotalMemoryBytes();
        log.debug("Métricas coletadas: CPU={}%, RAM={}MB/{} MB",
            String.format("%.1f", currentCpuUsage),
            currentMemoryUsed / 1_048_576,
            currentMemoryTotal / 1_048_576);
    }

    public double getCurrentCpuUsage() { return currentCpuUsage; }
    public long getCurrentMemoryUsed() { return currentMemoryUsed; }
    public long getCurrentMemoryTotal() { return currentMemoryTotal; }
}
