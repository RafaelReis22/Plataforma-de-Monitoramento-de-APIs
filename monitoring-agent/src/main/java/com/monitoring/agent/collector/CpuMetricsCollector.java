package com.monitoring.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class CpuMetricsCollector {

    private final CentralProcessor processor;
    private final boolean oshiAvailable;
    private final String procBasePath;

    private long[] prevTicks;

    public CpuMetricsCollector(
            @Value("${monitoring.proc.base-path:/proc}") String procBasePath) {
        this.procBasePath = procBasePath;
        CentralProcessor p = null;
        boolean available = false;
        try {
            SystemInfo si = new SystemInfo();
            p = si.getHardware().getProcessor();
            p.getSystemCpuLoadBetweenTicks(null); // probe
            this.prevTicks = p.getSystemCpuLoadTicks();
            available = true;
            log.info("OSHI CPU collector inicializado");
        } catch (UnsatisfiedLinkError | Exception e) {
            log.warn("OSHI indisponível — usando fallback /proc: {}", e.getMessage());
        }
        this.processor = p;
        this.oshiAvailable = available;
    }

    public double getCpuUsagePercent() {
        if (oshiAvailable && processor != null) {
            long[] ticks = processor.getSystemCpuLoadTicks();
            double load = processor.getSystemCpuLoadBetweenTicks(prevTicks);
            prevTicks = ticks;
            return load * 100.0;
        }
        return readCpuFromProc();
    }

    private double readCpuFromProc() {
        Path statPath = Path.of(procBasePath, "stat");
        try (BufferedReader reader = Files.newBufferedReader(statPath)) {
            String line = reader.readLine();
            if (line == null || !line.startsWith("cpu")) return -1.0;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) return -1.0;
            long user   = Long.parseLong(parts[1]);
            long nice   = Long.parseLong(parts[2]);
            long system = Long.parseLong(parts[3]);
            long idle   = Long.parseLong(parts[4]);
            long total  = user + nice + system + idle;
            return total == 0 ? 0.0 : (double)(total - idle) / total * 100.0;
        } catch (IOException e) {
            log.debug("Falha ao ler {}/stat: {}", procBasePath, e.getMessage());
            return -1.0;
        }
    }
}
