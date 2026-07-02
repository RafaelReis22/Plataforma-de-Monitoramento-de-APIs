package com.monitoring.storage.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.monitoring.storage.config.StorageConfig;

/**
 * Testes de integração do PrometheusQueryClient usando MockRestServiceServer.
 * Valida o mapeamento do JSON da API Prometheus para PrometheusResultado.
 */
@RestClientTest
@Import(StorageConfig.class)
class PrometheusQueryClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockRestServiceServer mockServer;

    private PrometheusQueryClient client;

    // Resposta Prometheus para query instantânea com 1 resultado do tipo vector
    private static final String RESPOSTA_VECTOR_JSON = """
        {
          "status": "success",
          "data": {
            "resultType": "vector",
            "result": [
              {
                "metric": { "job": "api-interceptor", "instance": "localhost:8082" },
                "value": [1750000000, "0.95"]
              }
            ]
          }
        }
        """;

    // Resposta Prometheus para query de intervalo com 2 pontos do tipo matrix
    private static final String RESPOSTA_MATRIX_JSON = """
        {
          "status": "success",
          "data": {
            "resultType": "matrix",
            "result": [
              {
                "metric": { "job": "storage-layer" },
                "values": [
                  [1750000000, "0.05"],
                  [1750000015, "0.08"]
                ]
              }
            ]
          }
        }
        """;

    // Resposta vazia — sem séries temporais para a query
    private static final String RESPOSTA_VAZIA_JSON = """
        {
          "status": "success",
          "data": {
            "resultType": "vector",
            "result": []
          }
        }
        """;

    @BeforeEach
    void configurar() {
        client = new PrometheusQueryClient(restTemplate, "http://localhost:9090");
    }

    @Test
    @DisplayName("consultar() retorna PrometheusResultado com labels e valor quando Prometheus responde vector")
    void consultar_retornaResultadoComLabelsEValor_quandoPrometheusRespondeVector() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/query")))
                  .andExpect(method(HttpMethod.GET))
                  .andExpect(queryParam("query", "up"))
                  .andRespond(withSuccess(RESPOSTA_VECTOR_JSON, MediaType.APPLICATION_JSON));

        List<PrometheusResultado> resultados = client.consultar("up");

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).labels()).containsEntry("job", "api-interceptor");
        assertThat(resultados.get(0).valores()).hasSize(1);
        assertThat(resultados.get(0).valores().get(0)[1]).isEqualTo(0.95);
    }

    @Test
    @DisplayName("consultar() retorna lista vazia quando Prometheus não tem dados para a query")
    void consultar_retornaListaVazia_quandoPrometheusNaoTemDados() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/query")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withSuccess(RESPOSTA_VAZIA_JSON, MediaType.APPLICATION_JSON));

        List<PrometheusResultado> resultados = client.consultar("metrica_inexistente");

        assertThat(resultados).isEmpty();
    }

    @Test
    @DisplayName("consultarIntervalo() retorna lista com múltiplos valores quando Prometheus responde matrix")
    void consultarIntervalo_retornaMultiplosValores_quandoPrometheusRespondeMatrix() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/query_range")))
                  .andExpect(method(HttpMethod.GET))
                  .andExpect(queryParam("query", "http_server_requests_seconds_count"))
                  .andRespond(withSuccess(RESPOSTA_MATRIX_JSON, MediaType.APPLICATION_JSON));

        Instant inicio = Instant.ofEpochSecond(1750000000L);
        Instant fim = Instant.ofEpochSecond(1750000060L);

        List<PrometheusResultado> resultados = client.consultarIntervalo(
                "http_server_requests_seconds_count", inicio, fim);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).labels()).containsEntry("job", "storage-layer");
        // matrix retorna múltiplos pontos de tempo
        assertThat(resultados.get(0).valores()).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("consultar() retorna lista vazia quando Prometheus retorna erro HTTP 5xx (degradação graciosa)")
    void consultar_retornaListaVazia_quandoPrometheusRetornaErroCincoXx() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/query")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withServerError());

        // Comportamento esperado: não lança exceção, retorna lista vazia
        List<PrometheusResultado> resultados = client.consultar("up");

        assertThat(resultados).isEmpty();
    }

    @Test
    @DisplayName("consultar() retorna lista vazia quando Prometheus está indisponível (degradação graciosa)")
    void consultar_retornaListaVazia_quandoPrometheusEstaIndisponivel() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/query")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withServiceUnavailable());

        List<PrometheusResultado> resultados = client.consultar("monitoring_cpu_usage_percent");

        assertThat(resultados).isEmpty();
    }
}
