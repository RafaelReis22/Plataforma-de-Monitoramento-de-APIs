package com.monitoring.interceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpMetricsInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "request.startTime";
    private final MeterRegistry meterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());

        // Trace Context W3C propagation & MDC logging context
        String traceParent = request.getHeader("traceparent");
        String traceId;
        String spanId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        if (traceParent != null && traceParent.startsWith("00-")) {
            String[] parts = traceParent.split("-");
            traceId = parts.length > 1 ? parts[1] : java.util.UUID.randomUUID().toString().replace("-", "");
        } else {
            traceId = java.util.UUID.randomUUID().toString().replace("-", "");
            traceParent = String.format("00-%s-%s-01", traceId, spanId);
        }

        org.slf4j.MDC.put("traceId", traceId);
        org.slf4j.MDC.put("spanId", spanId);
        response.setHeader("traceparent", traceParent);
        response.setHeader("X-Trace-ID", traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) return;

        long durationNanos = System.nanoTime() - startTime;
        String method = request.getMethod();
        String uri = sanitizeUri(request.getRequestURI());
        String status = String.valueOf(response.getStatus());
        String statusGroup = status.charAt(0) + "xx";

        Timer.builder("http.server.requests")
            .tag("method", method)
            .tag("uri", uri)
            .tag("status", status)
            .tag("outcome", resolveOutcome(response.getStatus()))
            .description("HTTP server request latency")
            .publishPercentileHistogram()
            .register(meterRegistry)
            .record(durationNanos, TimeUnit.NANOSECONDS);

        meterRegistry.counter("http.server.requests.total",
            "method", method,
            "uri", uri,
            "status_group", statusGroup
        ).increment();

        if (ex != null || response.getStatus() >= 500) {
            meterRegistry.counter("http.server.errors.total",
                "method", method,
                "uri", uri,
                "status", status
            ).increment();
        }

        org.slf4j.MDC.clear();
    }

    private String sanitizeUri(String uri) {
        // Remove path params numéricos para agrupar /api/users/123 → /api/users/{id}
        return uri.replaceAll("/\\d+", "/{id}");
    }

    private String resolveOutcome(int status) {
        return switch (status / 100) {
            case 2 -> "SUCCESS";
            case 3 -> "REDIRECTION";
            case 4 -> "CLIENT_ERROR";
            case 5 -> "SERVER_ERROR";
            default -> "UNKNOWN";
        };
    }
}
