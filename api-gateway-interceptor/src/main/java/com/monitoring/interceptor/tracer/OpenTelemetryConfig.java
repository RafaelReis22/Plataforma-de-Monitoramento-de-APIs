package com.monitoring.interceptor.tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class OpenTelemetryConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryConfig.class);

    public static class TraceContext {
        private final String traceId;
        private final String spanId;
        private final String parentSpanId;
        private final long startTimeMs;

        public TraceContext(String traceId, String spanId, String parentSpanId, long startTimeMs) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
            this.startTimeMs = startTimeMs;
        }

        public String getTraceId() { return traceId; }
        public String getSpanId() { return spanId; }
        public String getParentSpanId() { return parentSpanId; }
        public long getStartTimeMs() { return startTimeMs; }
    }

    public static TraceContext createOrExtractTraceContext(String traceparentHeader) {
        long now = System.currentTimeMillis();
        if (traceparentHeader != null && traceparentHeader.startsWith("00-")) {
            String[] parts = traceparentHeader.split("-");
            if (parts.length >= 4) {
                String traceId = parts[1];
                String parentSpanId = parts[2];
                String newSpanId = generateSpanId();
                return new TraceContext(traceId, newSpanId, parentSpanId, now);
            }
        }
        String traceId = generateTraceId();
        String spanId = generateSpanId();
        return new TraceContext(traceId, spanId, null, now);
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static String formatTraceparent(TraceContext ctx) {
        return String.format("00-%s-%s-01", ctx.getTraceId(), ctx.getSpanId());
    }
}
