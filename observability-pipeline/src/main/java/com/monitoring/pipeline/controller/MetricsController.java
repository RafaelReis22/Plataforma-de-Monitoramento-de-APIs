package com.monitoring.pipeline.controller;

import com.monitoring.pipeline.model.AggregatedMetric;
import com.monitoring.pipeline.model.MetricSnapshot;
import com.monitoring.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class MetricsController {

    private final PipelineService pipelineService;

    /** Ingere um novo snapshot bruto. */
    @PostMapping("/raw")
    public ResponseEntity<Map<String, String>> ingerirSnapshot(
            @RequestParam String chave,
            @RequestBody MetricSnapshot snapshot) {
        pipelineService.armazenarSnapshot(chave, snapshot);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Snapshot armazenado", "chave", chave));
    }

    /** Recupera um snapshot bruto pelo nome da chave. */
    @GetMapping("/raw/{chave}")
    public ResponseEntity<MetricSnapshot> obterSnapshot(@PathVariable String chave) {
        MetricSnapshot snapshot = pipelineService.obterSnapshot(chave);
        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshot);
    }

    /** Lista todos os agregados disponíveis no Redis. */
    @GetMapping("/agregadas")
    public ResponseEntity<List<AggregatedMetric>> listarAgregados() {
        return ResponseEntity.ok(pipelineService.listarAgregados());
    }

    /** Endpoint de saúde do pipeline. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servico", "observability-pipeline"
        ));
    }
}
