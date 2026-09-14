package com.monitoring.dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final Random random = new Random();

    @GetMapping("/hardware")
    public ResponseEntity<Map<String, Object>> getHardwareMetrics() {
        double cpuUsage = Math.round((28.5 + random.nextDouble() * 8.0) * 10.0) / 10.0;
        double ramUsage = Math.round((64.2 + random.nextDouble() * 3.0) * 10.0) / 10.0;
        double diskUsage = 52.4;

        Map<String, Object> response = new HashMap<>();
        response.put("cpuUsagePercentage", cpuUsage);
        response.put("cpuCores", 8);
        response.put("cpuLoadAverage", Arrays.asList(1.42, 1.28, 1.15));
        response.put("memoryUsedGb", 10.27);
        response.put("memoryTotalGb", 16.0);
        response.put("memoryUsagePercentage", ramUsage);
        response.put("swapUsedGb", 0.45);
        response.put("swapTotalGb", 4.0);
        response.put("diskUsedGb", 262.0);
        response.put("diskTotalGb", 500.0);
        response.put("diskUsagePercentage", diskUsage);
        response.put("networkRxKbps", Math.round(1240 + random.nextInt(350)));
        response.put("networkTxKbps", Math.round(890 + random.nextInt(200)));

        List<Map<String, Object>> cores = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cores.add(Map.of("coreId", i, "usage", Math.round((18.0 + random.nextDouble() * 25.0) * 10.0) / 10.0));
        }
        response.put("cores", cores);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api-health")
    public ResponseEntity<Map<String, Object>> getApiHealthMetrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalRequests", 1485290);
        response.put("requestsPerSecond", Math.round((342.0 + random.nextDouble() * 45.0) * 10.0) / 10.0);
        response.put("p50LatencyMs", 18);
        response.put("p90LatencyMs", 42);
        response.put("p95LatencyMs", 68);
        response.put("p99LatencyMs", 145);
        response.put("errorRatePercentage", 0.42);

        Map<String, Integer> statusDistribution = Map.of(
                "200_OK", 1420100,
                "201_CREATED", 54300,
                "400_BAD_REQUEST", 4200,
                "404_NOT_FOUND", 2100,
                "500_SERVER_ERROR", 410,
                "503_SERVICE_UNAVAILABLE", 180
        );
        response.put("statusDistribution", statusDistribution);

        List<Map<String, Object>> endpoints = List.of(
                Map.of("method", "GET", "path", "/api/v1/orders", "count", 482100, "p95Ms", 45, "errorRate", 0.12, "status", "HEALTHY"),
                Map.of("method", "POST", "path", "/api/v1/payments/checkout", "count", 192400, "p95Ms", 182, "errorRate", 1.84, "status", "WARNING"),
                Map.of("method", "GET", "path", "/api/v1/products/search", "count", 610900, "p95Ms", 32, "errorRate", 0.05, "status", "HEALTHY"),
                Map.of("method", "PUT", "path", "/api/v1/users/profile", "count", 89400, "p95Ms", 64, "errorRate", 0.31, "status", "HEALTHY"),
                Map.of("method", "DELETE", "path", "/api/v1/cart/item", "count", 34200, "p95Ms", 28, "errorRate", 0.08, "status", "HEALTHY")
        );
        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jvm")
    public ResponseEntity<Map<String, Object>> getJvmMetrics() {
        Map<String, Object> response = new HashMap<>();
        double heapUsed = Math.round((420.0 + random.nextDouble() * 60.0) * 10.0) / 10.0;
        response.put("heapUsedMb", heapUsed);
        response.put("heapMaxMb", 2048.0);
        response.put("heapCommittedMb", 1024.0);
        response.put("nonHeapUsedMb", 142.5);
        response.put("activeThreads", 48 + random.nextInt(8));
        response.put("peakThreads", 92);
        response.put("daemonThreads", 24);
        response.put("gcPauseTotalMs", 184.0);
        response.put("gcCollectionsCount", 12);
        response.put("loadedClasses", 14280);
        response.put("jvmUptimeHours", 168.4);
        response.put("javaVersion", "Java 21.0.4 (Eclipse Temurin)");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> response = new HashMap<>();
        response.put("criticalCount", 1);
        response.put("warningCount", 2);
        response.put("healthyCount", 14);

        List<Map<String, Object>> alerts = List.of(
                Map.of(
                        "id", "ALT-8902",
                        "name", "High P99 Latency Spike",
                        "service", "api-gateway-interceptor",
                        "severity", "CRITICAL",
                        "status", "FIRING",
                        "condition", "P99 Latency > 300ms por 5 min",
                        "currentValue", "345 ms",
                        "triggeredAt", "2026-09-14 16:02:10"
                ),
                Map.of(
                        "id", "ALT-8903",
                        "name", "JVM Heap Memory Usage > 80%",
                        "service", "storage-layer",
                        "severity", "WARNING",
                        "status", "FIRING",
                        "condition", "Heap > 80% por 10 min",
                        "currentValue", "84.2 %",
                        "triggeredAt", "2026-09-14 15:45:00"
                ),
                Map.of(
                        "id", "ALT-8901",
                        "name", "High CPU Load on Host",
                        "service", "monitoring-agent",
                        "severity", "WARNING",
                        "status", "RESOLVED",
                        "condition", "CPU > 80% por 5 min",
                        "currentValue", "32.1 %",
                        "triggeredAt", "2026-09-14 12:30:00"
                )
        );
        response.put("alerts", alerts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/synthetic")
    public ResponseEntity<Map<String, Object>> getSyntheticProbes() {
        Map<String, Object> response = new HashMap<>();
        response.put("overallAvailabilityPercentage", 99.96);
        response.put("errorBudgetRemainingPercentage", 84.5);

        List<Map<String, Object>> probes = List.of(
                Map.of("name", "Auth API Ping", "url", "https://api.monitoring.internal/v1/auth/health", "status", "UP", "latencyMs", 12, "sslDaysRemaining", 84, "uptime30d", 100.0),
                Map.of("name", "Checkout Payment Gateway Probe", "url", "https://api.monitoring.internal/v1/checkout", "status", "UP", "latencyMs", 142, "sslDaysRemaining", 120, "uptime30d", 99.92),
                Map.of("name", "Catalog Search Index Probe", "url", "https://api.monitoring.internal/v1/catalog", "status", "UP", "latencyMs", 24, "sslDaysRemaining", 45, "uptime30d", 99.99),
                Map.of("name", "Notifications Webhook Endpoint", "url", "https://api.monitoring.internal/v1/webhooks", "status", "DEGRADED", "latencyMs", 480, "sslDaysRemaining", 14, "uptime30d", 99.85)
        );
        response.put("probes", probes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/traces")
    public ResponseEntity<Map<String, Object>> getTraces(@RequestParam(value = "traceId", required = false) String traceId) {
        String targetTraceId = (traceId != null && !traceId.isEmpty()) ? traceId : "4c9b809a128e45f9a012345678abcdef";
        
        List<Map<String, Object>> spans = List.of(
                Map.of("spanId", "span-001", "service", "api-gateway-interceptor", "name", "HTTP GET /api/v1/orders", "durationMs", 42, "statusCode", "OK"),
                Map.of("spanId", "span-002", "service", "observability-pipeline", "name", "Redis Cache Ingestion", "durationMs", 12, "statusCode", "OK"),
                Map.of("spanId", "span-003", "service", "storage-layer", "name", "PostgreSQL Query SELECT", "durationMs", 18, "statusCode", "OK")
        );

        return ResponseEntity.ok(Map.of(
                "traceId", targetTraceId,
                "totalDurationMs", 72,
                "servicesCount", 3,
                "spans", spans
        ));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getCorrelatedLogs(@RequestParam(value = "traceId", required = false) String traceId) {
        String targetTraceId = (traceId != null && !traceId.isEmpty()) ? traceId : "4c9b809a128e45f9a012345678abcdef";

        List<Map<String, Object>> logs = List.of(
                Map.of("timestamp", "2026-09-14 16:15:02.120", "level", "INFO", "service", "api-gateway-interceptor", "traceId", targetTraceId, "message", "Recebida requisição HTTP GET /api/v1/orders"),
                Map.of("timestamp", "2026-09-14 16:15:02.132", "level", "INFO", "service", "observability-pipeline", "traceId", targetTraceId, "message", "Agregando métricas no Redis L2 Cache"),
                Map.of("timestamp", "2026-09-14 16:15:02.150", "level", "INFO", "service", "storage-layer", "traceId", targetTraceId, "message", "Consulta SQL executada em 18ms no PostgreSQL")
        );

        return ResponseEntity.ok(logs);
    }
}
