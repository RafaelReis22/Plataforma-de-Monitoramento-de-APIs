package com.monitoring.agent.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final HardwareMetricsService hardwareMetricsService;

    @GetMapping("/snapshot")
    public ResponseEntity<Map<String, Object>> getSnapshot() {
        MetricSnapshot s = hardwareMetricsService.getLatestSnapshot();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "timestamp", s.timestamp().toString(),
                "cpu", Map.of(
                    "usagePercent", s.cpuUsagePercent()
                ),
                "memory", Map.of(
                    "usedBytes", s.memoryUsedBytes(),
                    "totalBytes", s.memoryTotalBytes(),
                    "usedPercent", s.memoryUsedPercent()
                ),
                "network", Map.of(
                    "bytesReceived", s.networkBytesReceived(),
                    "bytesSent", s.networkBytesSent()
                ),
                "disk", Map.of(
                    "readBytes", s.diskReadBytes(),
                    "writeBytes", s.diskWriteBytes(),
                    "usedBytes", s.diskUsedBytes(),
                    "totalBytes", s.diskTotalBytes()
                )
            ),
            "error", null
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "agent", "monitoring-agent"));
    }
}
