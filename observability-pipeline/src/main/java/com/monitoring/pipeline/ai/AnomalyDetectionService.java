package com.monitoring.pipeline.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);
    private static final double Z_SCORE_THRESHOLD = 3.0;

    public static class AnomalyResult {
        private final boolean anomaly;
        private final double value;
        private final double mean;
        private final double zScore;

        public AnomalyResult(boolean anomaly, double value, double mean, double zScore) {
            this.anomaly = anomaly;
            this.value = value;
            this.mean = mean;
            this.zScore = zScore;
        }

        public boolean isAnomaly() { return anomaly; }
        public double getValue() { return value; }
        public double getMean() { return mean; }
        public double getZScore() { return zScore; }
    }

    public AnomalyResult detectAnomaly(double currentValue, List<Double> historicalWindow) {
        if (historicalWindow == null || historicalWindow.isEmpty()) {
            return new AnomalyResult(false, currentValue, currentValue, 0.0);
        }

        double sum = 0.0;
        for (double val : historicalWindow) {
            sum += val;
        }
        double mean = sum / historicalWindow.size();

        double varianceSum = 0.0;
        for (double val : historicalWindow) {
            varianceSum += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / historicalWindow.size());

        if (stdDev == 0.0) {
            return new AnomalyResult(false, currentValue, mean, 0.0);
        }

        double zScore = (currentValue - mean) / stdDev;
        boolean isAnomaly = Math.abs(zScore) >= Z_SCORE_THRESHOLD;

        if (isAnomaly) {
            log.warn("[AIOps Anomaly] Anomalia estatística detectada! Valor: {}, Média: {}, Z-Score: {}", currentValue, mean, zScore);
        }

        return new AnomalyResult(isAnomaly, currentValue, mean, zScore);
    }
}
