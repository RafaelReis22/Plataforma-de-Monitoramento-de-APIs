package com.monitoring.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FinOpsMeteringService {

    private static final Logger log = LoggerFactory.getLogger(FinOpsMeteringService.class);
    private static final double COST_PER_GB_INGESTED = 0.15; // USD por GB de dados de observabilidade

    public static class IngestionMetrics {
        private final String tenantId;
        private final double bytesPerSec;
        private final double dailyVolumeGb;
        private final double estimatedMonthlyCostUsd;

        public IngestionMetrics(String tenantId, double bytesPerSec, double dailyVolumeGb, double estimatedMonthlyCostUsd) {
            this.tenantId = tenantId;
            this.bytesPerSec = bytesPerSec;
            this.dailyVolumeGb = dailyVolumeGb;
            this.estimatedMonthlyCostUsd = estimatedMonthlyCostUsd;
        }

        public String getTenantId() { return tenantId; }
        public double getBytesPerSec() { return bytesPerSec; }
        public double getDailyVolumeGb() { return dailyVolumeGb; }
        public double getEstimatedMonthlyCostUsd() { return estimatedMonthlyCostUsd; }
    }

    public IngestionMetrics calculateTenantCost(String tenantId, long payloadSizeBytes) {
        double bytesPerSec = payloadSizeBytes * 100.0;
        double dailyVolumeGb = (bytesPerSec * 86400.0) / (1024.0 * 1024.0 * 1024.0);
        double monthlyCost = dailyVolumeGb * 30.0 * COST_PER_GB_INGESTED;

        log.info("[FinOps] Ingestão Tenant '{}': {} B/s, Est. Mensal: ${}", tenantId, bytesPerSec, String.format("%.2f", monthlyCost));

        return new IngestionMetrics(tenantId, bytesPerSec, dailyVolumeGb, monthlyCost);
    }
}
