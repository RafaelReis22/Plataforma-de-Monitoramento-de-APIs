package com.monitoring.dashboard.controller;

import com.monitoring.dashboard.client.PrometheusValidationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final PrometheusValidationClient validationClient;

    /**
     * GET /api/dashboards
     * Lista os nomes dos dashboards disponíveis na pasta de recursos
     */
    @GetMapping
    public ResponseEntity<List<String>> listarDashboards() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:dashboards/*.json");
            List<String> nomes = Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .map(name -> name.substring(0, name.lastIndexOf('.')))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(nomes);
        } catch (IOException e) {
            log.error("Erro ao listar dashboards", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/dashboards/{id}
     * Serve a configuração JSON do dashboard solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> obterDashboard(@PathVariable("id") String id) {
        // Validação simples para evitar Path Traversal
        if (id == null || !id.matches("^[a-zA-Z0-9-_]+$")) {
            return ResponseEntity.badRequest().body("{\"erro\":\"Nome de dashboard inválido\"}");
        }

        try {
            Resource resource = new ClassPathResource("dashboards/" + id + ".json");
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"erro\":\"Dashboard não encontrado\"}");
            }
            String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (IOException e) {
            log.error("Erro ao ler dashboard: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"erro\":\"Erro interno ao ler arquivo de dashboard\"}");
        }
    }

    /**
     * POST /api/dashboards/validate-query
     * Valida sintaxe e validade de expressões PromQL usando a API do Prometheus
     */
    @PostMapping("/validate-query")
    public ResponseEntity<ValidationResponse> validarQuery(@RequestBody ValidationRequest request) {
        if (request == null || request.query() == null || request.query().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ValidationResponse(false, "Query vazia"));
        }

        StringBuilder erroMsg = new StringBuilder();
        boolean valido = validationClient.validarQuery(request.query(), erroMsg);

        if (valido) {
            return ResponseEntity.ok(new ValidationResponse(true, "Query válida"));
        } else {
            return ResponseEntity.ok(new ValidationResponse(false, erroMsg.toString()));
        }
    }

    // DTOs como records do Java 21
    public record ValidationRequest(String query) {}
    public record ValidationResponse(boolean valido, String mensagem) {}
}
