package com.monitoring.storage.controller;

import com.monitoring.storage.client.PrometheusResultado;
import com.monitoring.storage.exception.MetricNaoEncontradaException;
import com.monitoring.storage.model.MetricRecord;
import com.monitoring.storage.service.MetricStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Endpoints REST para consulta e persistência de histórico de métricas
@RestController
@RequestMapping("/api/historico")
@Slf4j
@RequiredArgsConstructor
public class MetricHistoryController {

    private final MetricStorageService storageService;

    // POST /api/historico — persiste um novo registro de métrica
    @PostMapping
    public ResponseEntity<MetricRecord> salvar(@RequestBody MetricRecord registro) {
        MetricRecord salvo = storageService.salvar(registro);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // GET /api/historico/{nome} — retorna histórico completo de uma métrica
    @GetMapping("/{nome}")
    public ResponseEntity<?> buscarPorNome(@PathVariable("nome") String nome) {
        try {
            List<MetricRecord> registros = storageService.buscarPorNome(nome);
            return ResponseEntity.ok(registros);
        } catch (MetricNaoEncontradaException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }

    // GET /api/historico/{nome}/intervalo?inicio=...&fim=... — histórico por período
    @GetMapping("/{nome}/intervalo")
    public ResponseEntity<List<MetricRecord>> buscarPorIntervalo(
            @PathVariable("nome") String nome,
            @RequestParam("inicio") Instant inicio,
            @RequestParam("fim") Instant fim) {
        List<MetricRecord> registros = storageService.buscarPorIntervalo(nome, inicio, fim);
        return ResponseEntity.ok(registros);
    }

    // GET /api/historico/registro/{id} — busca um registro específico pelo ID
    @GetMapping("/registro/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(storageService.buscarPorId(id));
        } catch (MetricNaoEncontradaException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }

    // GET /api/historico/prometheus?query={promql} — consulta Prometheus PromQL
    @GetMapping("/prometheus")
    public ResponseEntity<List<PrometheusResultado>> consultarPrometheus(
            @RequestParam("query") String query) {
        List<PrometheusResultado> resultados = storageService.consultarPrometheus(query);
        return ResponseEntity.ok(resultados);
    }

    // GET /api/historico/prometheus/range?query=...&inicio=...&fim=... — série temporal PromQL
    @GetMapping("/prometheus/range")
    public ResponseEntity<List<PrometheusResultado>> consultarPrometheusRange(
            @RequestParam("query") String query,
            @RequestParam("inicio") Instant inicio,
            @RequestParam("fim") Instant fim) {
        List<PrometheusResultado> resultados = storageService.consultarPrometheusIntervalo(query, inicio, fim);
        return ResponseEntity.ok(resultados);
    }

    // GET /api/historico/health — verificação de saúde do serviço
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servico", "storage-layer"
        ));
    }
}
