package com.monitoring.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MemoryMetricsCollector {

    private final GlobalMemory memory;
    private final boolean oshiAvailable;
    private final String procBasePath;

    public MemoryMetricsCollector(
            @Value("${monitoring.proc.base-path:/proc}") String procBasePath) {
        this.procBasePath = procBasePath;
        GlobalMemory mem = null;
        boolean available = false;
        try {
            SystemInfo si = new SystemInfo();
            mem = si.getHardware().getMemory();
            mem.getTotal(); // probe
            available = true;
            log.info("OSHI Memory collector inicializado");
        } catch (UnsatisfiedLinkError | Exception e) {
            log.warn("OSHI indisponível para memória — usando /proc/meminfo: {}", e.getMessage());
        }
        this.memory = mem;
        this.oshiAvailable = available;
    }

    public long getTotalMemoryBytes() {
        if (oshiAvailable && memory != null) return memory.getTotal();
        return readMemInfo().getOrDefault("MemTotal", 0L);
    }

    public long getUsedMemoryBytes() {
        if (oshiAvailable && memory != null) {
            return memory.getTotal() - memory.getAvailable();
        }
        Map<String, Long> info = readMemInfo();
        long total = info.getOrDefault("MemTotal", 0L);
        long avail = info.getOrDefault("MemAvailable", 0L);
        return total - avail;
    }

    public double getUsedMemoryPercent() {
        long total = getTotalMemoryBytes();
        if (total == 0) return 0.0;
        return (double) getUsedMemoryBytes() / total * 100.0;
    }

    private Map<String, Long> readMemInfo() {
        Map<String, Long> result = new HashMap<>();
        Path memPath = Path.of(procBasePath, "meminfo");
        try (BufferedReader reader = Files.newBufferedReader(memPath)) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(":\\s+");
                if (parts.length == 2) {
                    String value = parts[1].replace(" kB", "").trim();
                    try {
                        result.put(parts[0].trim(), Long.parseLong(value) * 1024L);
                    } catch (NumberFormatException ignored) {}
                }
            });
        } catch (IOException e) {
            log.debug("Falha ao ler {}/meminfo: {}", procBasePath, e.getMessage());
        }
        return result;
    }
}
