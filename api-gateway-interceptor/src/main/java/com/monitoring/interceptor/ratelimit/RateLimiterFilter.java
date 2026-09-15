package com.monitoring.interceptor.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 5000;
    private final Map<String, AtomicInteger> tenantRequestCounters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String tenantId = req.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "default-tenant";
        }

        AtomicInteger counter = tenantRequestCounters.computeIfAbsent(tenantId, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("[RateLimiter] Limite de requisições excedido para o Tenant '{}': {}", tenantId, currentCount);
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit de ingestão excedido para este Tenant.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
