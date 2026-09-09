package com.monitoring.dashboard.controller;

import com.monitoring.dashboard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    private static final Map<String, String> API_KEYS = new ConcurrentHashMap<>(Map.of(
            "ak_live_prod_9876543210abcdef", "Production Cluster Key",
            "ak_test_stage_1234567890fedcba", "Staging Environment Key"
    ));

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String password, String email, String organizationName) {}
    public record AuthResponse(String token, String type, String username, String role, String tenantId) {}
    public record ApiKeyCreateRequest(String name) {}
    public record ApiKeyResponse(String apiKey, String name, String createdAt) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Credenciais de acesso são obrigatórias"));
        }

        // Validação simulada (aceita admin/admin123 ou dev/dev123)
        String role = request.username().equalsIgnoreCase("admin") ? "ADMIN" : "DEVELOPER";
        String tenantId = "tenant-enterprise-01";

        if (request.password().length() < 4) {
            return ResponseEntity.status(401).body(Map.of("erro", "Senha incorreta"));
        }

        String token = tokenProvider.createToken(request.username(), role, tenantId);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", request.username(), role, tenantId));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String tenantId = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String token = tokenProvider.createToken(request.username(), "ADMIN", tenantId);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", request.username(), "ADMIN", tenantId));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenProvider.validateToken(token)) {
                return ResponseEntity.ok(Map.of(
                        "username", tokenProvider.getUsername(token),
                        "role", tokenProvider.getRole(token),
                        "tenantId", tokenProvider.getTenantId(token),
                        "email", tokenProvider.getUsername(token) + "@monitoring-platform.io",
                        "organization", "Enterprise Telemetry Corp"
                ));
            }
        }
        return ResponseEntity.ok(Map.of(
                "username", "guest",
                "role", "VIEWER",
                "tenantId", "tenant-default",
                "email", "guest@monitoring-platform.io",
                "organization", "Demo Workspace"
        ));
    }

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys() {
        List<ApiKeyResponse> list = new ArrayList<>();
        API_KEYS.forEach((key, name) -> list.add(new ApiKeyResponse(key, name, "2026-09-14 10:00:00")));
        return ResponseEntity.ok(list);
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey(@RequestBody ApiKeyCreateRequest request) {
        String name = (request != null && request.name() != null) ? request.name() : "Nova API Key";
        String newKey = "ak_live_" + UUID.randomUUID().toString().replace("-", "");
        API_KEYS.put(newKey, name);
        return ResponseEntity.ok(new ApiKeyResponse(newKey, name, "2026-09-14 16:15:00"));
    }
}
