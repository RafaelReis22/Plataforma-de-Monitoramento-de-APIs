package com.monitoring.dashboard.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@Slf4j
public class PrometheusValidationClient {

    private final RestTemplate restTemplate;
    private final String prometheusUrl;

    public PrometheusValidationClient(RestTemplate restTemplate,
                                      @Value("${prometheus.url:http://localhost:9090}") String prometheusUrl) {
        this.restTemplate = restTemplate;
        this.prometheusUrl = prometheusUrl;
    }

    public boolean validarQuery(String promql, StringBuilder erroMsg) {
        String url = UriComponentsBuilder.fromHttpUrl(prometheusUrl)
                .path("/api/v1/query")
                .queryParam("query", promql)
                .toUriString();
        try {
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (HttpStatusCodeException ex) {
            log.warn("Erro ao validar query '{}' no Prometheus: {}", promql, ex.getMessage());
            try {
                String body = ex.getResponseBodyAsString();
                erroMsg.append(body != null && !body.isEmpty() ? body : ex.getMessage());
            } catch (Exception e) {
                erroMsg.append(ex.getMessage());
            }
            return false;
        } catch (Exception ex) {
            log.error("Falha inesperada ao comunicar com Prometheus para validar query: {}", ex.getMessage());
            erroMsg.append("Erro de conexão com o Prometheus: ").append(ex.getMessage());
            return false;
        }
    }
}
