package com.monitoring.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public class NetworkMetricsCollector {

    private final List<NetworkIF> networkIFs;
    private final boolean oshiAvailable;
    private final String procBasePath;

    private long[] prevRxBytes;
    private long[] prevTxBytes;
    private long prevTimestamp;

    public NetworkMetricsCollector(
            @Value("${monitoring.proc.base-path:/proc}") String procBasePath) {
        this.procBasePath = procBasePath;
        List<NetworkIF> ifaces = List.of();
        boolean available = false;
        try {
            SystemInfo si = new SystemInfo();
            ifaces = si.getHardware().getNetworkIFs();
            available = !ifaces.isEmpty();
            if (available) {
                prevRxBytes = ifaces.stream().mapToLong(NetworkIF::getBytesRecv).toArray();
                prevTxBytes = ifaces.stream().mapToLong(NetworkIF::getBytesSent).toArray();
                prevTimestamp = System.currentTimeMillis();
                log.info("OSHI Network collector: {} interfaces", ifaces.size());
            }
        } catch (UnsatisfiedLinkError | Exception e) {
            log.warn("OSHI indisponível para network — usando fallback /proc/net/dev: {}", e.getMessage());
        }
        this.networkIFs = ifaces;
        this.oshiAvailable = available;
    }

    public long getTotalBytesReceived() {
        if (oshiAvailable) {
            networkIFs.forEach(NetworkIF::updateAttributes);
            return networkIFs.stream().mapToLong(NetworkIF::getBytesRecv).sum();
        }
        return readNetDevStat(1); // coluna 1 = bytes recebidos
    }

    public long getTotalBytesSent() {
        if (oshiAvailable) {
            return networkIFs.stream().mapToLong(NetworkIF::getBytesSent).sum();
        }
        return readNetDevStat(9); // coluna 9 = bytes enviados
    }

    public double getReceiveRateBytesPerSec() {
        if (!oshiAvailable) return -1.0;
        networkIFs.forEach(NetworkIF::updateAttributes);
        long now = System.currentTimeMillis();
        long elapsedMs = now - prevTimestamp;
        if (elapsedMs <= 0) return 0.0;

        long totalRx = 0;
        for (int i = 0; i < networkIFs.size(); i++) {
            long current = networkIFs.get(i).getBytesRecv();
            totalRx += Math.max(0, current - prevRxBytes[i]);
            prevRxBytes[i] = current;
        }
        prevTimestamp = now;
        return (double) totalRx / elapsedMs * 1000.0;
    }

    private long readNetDevStat(int column) {
        Path netDev = Path.of(procBasePath, "net", "dev");
        long total = 0;
        try (BufferedReader reader = Files.newBufferedReader(netDev)) {
            // Pula 2 linhas de cabeçalho
            reader.readLine();
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("[:\\s]+");
                if (parts.length > column && !parts[0].equals("lo")) {
                    try {
                        total += Long.parseLong(parts[column]);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            log.debug("Falha ao ler {}/net/dev: {}", procBasePath, e.getMessage());
        }
        return total;
    }
}
