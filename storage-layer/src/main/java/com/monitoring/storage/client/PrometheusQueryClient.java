package com.monitoring.storage.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PrometheusQueryClient {

    private final RestTemplate restTemplate;
    private final String prometheusUrl;

    public PrometheusQueryClient(RestTemplate restTemplate,
                                 @Value("${prometheus.url:http://localhost:9090}") String prometheusUrl) {
        this.restTemplate = restTemplate;
        this.prometheusUrl = prometheusUrl;
    }

    // Consulta instantânea — retorna o valor atual de uma expressão PromQL
    @SuppressWarnings("unchecked")
    public List<PrometheusResultado> consultar(String promql) {
        String url = UriComponentsBuilder.fromHttpUrl(prometheusUrl)
                .path("/api/v1/query")
                .queryParam("query", promql)
                .toUriString();

        try {
            Map<String, Object> resposta = restTemplate.getForObject(url, Map.class);
            return extrairResultados(resposta, false);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar Prometheus (query={}): {}", promql, ex.getMessage());
            return Collections.emptyList();
        }
    }

    // Consulta por intervalo — retorna série temporal de uma expressão PromQL
    @SuppressWarnings("unchecked")
    public List<PrometheusResultado> consultarIntervalo(String promql, Instant inicio, Instant fim) {
        String url = UriComponentsBuilder.fromHttpUrl(prometheusUrl)
                .path("/api/v1/query_range")
                .queryParam("query", promql)
                .queryParam("start", inicio.getEpochSecond())
                .queryParam("end", fim.getEpochSecond())
                .queryParam("step", "15s")
                .toUriString();

        try {
            Map<String, Object> resposta = restTemplate.getForObject(url, Map.class);
            return extrairResultados(resposta, true);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar Prometheus range (query={}): {}", promql, ex.getMessage());
            return Collections.emptyList();
        }
    }

    // Extrai a lista de resultados da resposta JSON do Prometheus
    @SuppressWarnings("unchecked")
    private List<PrometheusResultado> extrairResultados(Map<String, Object> resposta, boolean intervalo) {
        if (resposta == null || !"success".equals(resposta.get("status"))) {
            return Collections.emptyList();
        }

        var data = (Map<String, Object>) resposta.get("data");
        if (data == null) {
            return Collections.emptyList();
        }

        var resultados = (List<Map<String, Object>>) data.get("result");
        if (resultados == null || resultados.isEmpty()) {
            return Collections.emptyList();
        }

        List<PrometheusResultado> saida = new ArrayList<>();
        for (var item : resultados) {
            var labels = (Map<String, String>) item.getOrDefault("metric", Map.of());
            var valores = new ArrayList<double[]>();

            if (intervalo) {
                // query_range: "values": [[ts, "valor"], ...]
                var values = (List<List<Object>>) item.get("values");
                if (values != null) {
                    for (var v : values) {
                        double ts = ((Number) v.get(0)).doubleValue();
                        double val = Double.parseDouble((String) v.get(1));
                        valores.add(new double[]{ts, val});
                    }
                }
            } else {
                // query: "value": [ts, "valor"]
                var value = (List<Object>) item.get("value");
                if (value != null && value.size() == 2) {
                    double ts = ((Number) value.get(0)).doubleValue();
                    double val = Double.parseDouble((String) value.get(1));
                    valores.add(new double[]{ts, val});
                }
            }

            saida.add(PrometheusResultado.de(labels, valores));
        }

        return saida;
    }
}
