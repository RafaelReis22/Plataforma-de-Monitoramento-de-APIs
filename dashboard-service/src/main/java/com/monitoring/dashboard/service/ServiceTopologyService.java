package com.monitoring.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceTopologyService {

    public static class ServiceNode {
        private final String id;
        private final String name;
        private final String type;
        private final String status;
        private final double rps;
        private final double errorRate;

        public ServiceNode(String id, String name, String type, String status, double rps, double errorRate) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.status = status;
            this.rps = rps;
            this.errorRate = errorRate;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public double getRps() { return rps; }
        public double getErrorRate() { return errorRate; }
    }

    public static class ServiceEdge {
        private final String source;
        private final String target;
        private final double callRate;
        private final double avgLatencyMs;

        public ServiceEdge(String source, String target, double callRate, double avgLatencyMs) {
            this.source = source;
            this.target = target;
            this.callRate = callRate;
            this.avgLatencyMs = avgLatencyMs;
        }

        public String getSource() { return source; }
        public String getTarget() { return target; }
        public double getCallRate() { return callRate; }
        public double getAvgLatencyMs() { return avgLatencyMs; }
    }

    public Map<String, Object> getTopologyGraph() {
        List<ServiceNode> nodes = Arrays.asList(
                new ServiceNode("gw-01", "api-gateway-interceptor", "GATEWAY", "HEALTHY", 1250.0, 0.2),
                new ServiceNode("pipeline-01", "observability-pipeline", "PIPELINE", "HEALTHY", 1250.0, 0.1),
                new ServiceNode("storage-01", "storage-layer", "DATABASE_SERVICE", "HEALTHY", 850.0, 0.0),
                new ServiceNode("agent-01", "monitoring-agent", "AGENT", "HEALTHY", 300.0, 0.0),
                new ServiceNode("dash-01", "dashboard-service", "API_SERVER", "HEALTHY", 450.0, 0.05),
                new ServiceNode("postgres-01", "PostgreSQL / TimescaleDB", "DATABASE", "HEALTHY", 850.0, 0.0),
                new ServiceNode("redis-01", "Redis Cache L2", "CACHE", "HEALTHY", 2100.0, 0.0)
        );

        List<ServiceEdge> edges = Arrays.asList(
                new ServiceEdge("gw-01", "pipeline-01", 1250.0, 12.4),
                new ServiceEdge("gw-01", "redis-01", 2100.0, 1.8),
                new ServiceEdge("pipeline-01", "storage-01", 850.0, 24.5),
                new ServiceEdge("storage-01", "postgres-01", 850.0, 8.2),
                new ServiceEdge("agent-01", "pipeline-01", 300.0, 5.1),
                new ServiceEdge("dash-01", "postgres-01", 450.0, 6.5)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }
}
