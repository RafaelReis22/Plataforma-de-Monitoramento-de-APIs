package com.monitoring.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public class DiskMetricsCollector {

    private final List<HWDiskStore> diskStores;
    private final List<OSFileStore> fileStores;
    private final boolean oshiAvailable;
    private final String procBasePath;

    public DiskMetricsCollector(
            @Value("${monitoring.proc.base-path:/proc}") String procBasePath) {
        this.procBasePath = procBasePath;
        List<HWDiskStore> disks = List.of();
        List<OSFileStore> files = List.of();
        boolean available = false;
        try {
            SystemInfo si = new SystemInfo();
            disks = si.getHardware().getDiskStores();
            files = si.getOperatingSystem().getFileSystem().getFileStores();
            available = !disks.isEmpty();
            log.info("OSHI Disk collector: {} discos, {} partições", disks.size(), files.size());
        } catch (UnsatisfiedLinkError | Exception e) {
            log.warn("OSHI indisponível para disco — usando fallback /proc/diskstats: {}", e.getMessage());
        }
        this.diskStores = disks;
        this.fileStores = files;
        this.oshiAvailable = available;
    }

    public long getTotalReadBytes() {
        if (oshiAvailable) {
            diskStores.forEach(HWDiskStore::updateAttributes);
            return diskStores.stream().mapToLong(HWDiskStore::getReadBytes).sum();
        }
        return readDiskStat(5); // coluna 5 do /proc/diskstats = setores lidos × 512
    }

    public long getTotalWriteBytes() {
        if (oshiAvailable) {
            return diskStores.stream().mapToLong(HWDiskStore::getWriteBytes).sum();
        }
        return readDiskStat(9); // coluna 9 do /proc/diskstats = setores gravados × 512
    }

    public long getTotalDiskSpaceBytes() {
        if (oshiAvailable && !fileStores.isEmpty()) {
            return fileStores.stream().mapToLong(OSFileStore::getTotalSpace).sum();
        }
        return -1L;
    }

    public long getUsedDiskSpaceBytes() {
        if (oshiAvailable && !fileStores.isEmpty()) {
            return fileStores.stream()
                .mapToLong(fs -> fs.getTotalSpace() - fs.getUsableSpace()).sum();
        }
        return -1L;
    }

    private long readDiskStat(int column) {
        Path diskstats = Path.of(procBasePath, "diskstats");
        long total = 0;
        try (BufferedReader reader = Files.newBufferedReader(diskstats)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                // Ignora partições (sda1, sdb1...) — processa apenas discos físicos (sda, sdb)
                if (parts.length > column && parts[2].matches("[a-z]+")) {
                    try {
                        total += Long.parseLong(parts[column]) * 512L;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            log.debug("Falha ao ler {}/diskstats: {}", procBasePath, e.getMessage());
        }
        return total;
    }
}
