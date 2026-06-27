package com.monitoring.storage.service;

import com.monitoring.storage.client.PrometheusQueryClient;
import com.monitoring.storage.client.PrometheusResultado;
import com.monitoring.storage.exception.MetricNaoEncontradaException;
import com.monitoring.storage.model.MetricRecord;
import com.monitoring.storage.repository.MetricRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// Serviço responsável pela persistência e consulta histórica de métricas
@Service
@Slf4j
@RequiredArgsConstructor
public class MetricStorageService {

    private final MetricRecordRepository repository;
    private final PrometheusQueryClient prometheusClient;

    // Persiste um novo registro de métrica no PostgreSQL
    @Transactional
    public MetricRecord salvar(MetricRecord registro) {
        if (registro.getTimestamp() == null) {
            registro.setTimestamp(Instant.now());
        }
        MetricRecord salvo = repository.save(registro);
        log.debug("Métrica salva: nome={}, valor={}, id={}", salvo.getNome(), salvo.getValor(), salvo.getId());
        return salvo;
    }

    // Busca todos os registros históricos de uma métrica pelo nome
    @Transactional(readOnly = true)
    public List<MetricRecord> buscarPorNome(String nome) {
        List<MetricRecord> registros = repository.findByNomeOrderByTimestampDesc(nome);
        if (registros.isEmpty()) {
            throw new MetricNaoEncontradaException(nome);
        }
        return registros;
    }

    // Busca registros de uma métrica dentro de um intervalo de tempo
    @Transactional(readOnly = true)
    public List<MetricRecord> buscarPorIntervalo(String nome, Instant inicio, Instant fim) {
        return repository.findByNomeAndTimestampBetweenOrderByTimestampAsc(nome, inicio, fim);
    }

    // Busca um registro específico pelo ID
    @Transactional(readOnly = true)
    public MetricRecord buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MetricNaoEncontradaException(id));
    }

    // Consulta o valor atual de uma expressão PromQL no Prometheus
    public List<PrometheusResultado> consultarPrometheus(String promql) {
        log.debug("Consultando Prometheus: {}", promql);
        return prometheusClient.consultar(promql);
    }

    // Consulta série temporal PromQL no Prometheus dentro de um intervalo
    public List<PrometheusResultado> consultarPrometheusIntervalo(String promql, Instant inicio, Instant fim) {
        log.debug("Consultando Prometheus range: query={}, inicio={}, fim={}", promql, inicio, fim);
        return prometheusClient.consultarIntervalo(promql, inicio, fim);
    }

    // Retorna quantos registros existem para um nome de métrica
    @Transactional(readOnly = true)
    public long contarPorNome(String nome) {
        return repository.countByNome(nome);
    }
}
