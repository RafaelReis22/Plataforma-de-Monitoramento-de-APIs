package com.monitoring.storage.e2e;

import com.monitoring.storage.client.PrometheusQueryClient;
import com.monitoring.storage.model.MetricRecord;
import com.monitoring.storage.repository.MetricRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes E2E do MetricHistoryController com servidor HTTP real, PostgreSQL real
 * via Testcontainers e PrometheusQueryClient mockado.
 * Valida o fluxo completo: HTTP → Controller → Service → Repository → PostgreSQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MetricHistoryE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("monitoring_e2e")
                    .withUsername("e2e_user")
                    .withPassword("e2e_pass");

    @DynamicPropertySource
    static void configurarPropriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("prometheus.url", () -> "http://localhost:9090");
    }

    @LocalServerPort
    int porta;

    @Autowired
    TestRestTemplate http;

    @Autowired
    MetricRecordRepository repository;

    // Isola os testes E2E do Prometheus real — testamos o HTTP client separadamente
    @MockBean
    PrometheusQueryClient prometheusClient;

    @BeforeEach
    void preparar() {
        when(prometheusClient.consultar(anyString())).thenReturn(List.of());
        when(prometheusClient.consultarIntervalo(anyString(), any(), any())).thenReturn(List.of());
    }

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    private String url(String caminho) {
        return "http://localhost:" + porta + caminho;
    }

    @Test
    @DisplayName("POST /api/historico retorna 201 com o registro criado")
    void post_retornaCriadoComRegistro_quandoPayloadValido() {
        MetricRecord payload = MetricRecord.builder()
                .nome("cpu_usage")
                .valor(42.5)
                .unidade("percent")
                .fonte("monitoring-agent")
                .build();

        ResponseEntity<MetricRecord> resposta = http.postForEntity(
                url("/api/historico"), payload, MetricRecord.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getId()).isNotNull();
        assertThat(resposta.getBody().getNome()).isEqualTo("cpu_usage");
        assertThat(resposta.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/historico/{nome} retorna lista com registros persistidos")
    void get_retornaListaComRegistros_quandoNomeExisteNoBanco() {
        // Cria 3 registros no banco
        for (int i = 1; i <= 3; i++) {
            http.postForEntity(url("/api/historico"),
                    MetricRecord.builder()
                            .nome("memoria_usada")
                            .valor(i * 100.0)
                            .fonte("monitoring-agent")
                            .build(),
                    MetricRecord.class);
        }

        ResponseEntity<List<MetricRecord>> resposta = http.exchange(
                url("/api/historico/memoria_usada"),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).hasSize(3);
    }

    @Test
    @DisplayName("GET /api/historico/{nome} retorna 404 quando métrica não existe")
    void get_retorna404_quandoMetricaNaoExiste() {
        ResponseEntity<Map<String, Object>> resposta = http.exchange(
                url("/api/historico/metrica_que_nao_existe"),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsKey("erro");
    }

    @Test
    @DisplayName("GET /api/historico/{nome}/intervalo filtra corretamente por timestamp")
    void getIntervalo_filtraCorretamente_quandoRangeDefinido() {
        Instant agora = Instant.now();

        // Registro dentro do intervalo (1 hora atrás)
        http.postForEntity(url("/api/historico"),
                MetricRecord.builder()
                        .nome("latencia_p99")
                        .valor(0.234)
                        .timestamp(agora.minusSeconds(3600))
                        .fonte("api-interceptor")
                        .build(),
                MetricRecord.class);

        // Registro fora do intervalo (2 dias atrás)
        http.postForEntity(url("/api/historico"),
                MetricRecord.builder()
                        .nome("latencia_p99")
                        .valor(0.500)
                        .timestamp(agora.minusSeconds(172800))
                        .fonte("api-interceptor")
                        .build(),
                MetricRecord.class);

        String inicio = agora.minusSeconds(7200).toString();
        String fim = agora.toString();

        ResponseEntity<List<MetricRecord>> resposta = http.exchange(
                url("/api/historico/latencia_p99/intervalo?inicio=" + inicio + "&fim=" + fim),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).hasSize(1);
        assertThat(resposta.getBody().get(0).getValor()).isEqualTo(0.234);
    }

    @Test
    @DisplayName("GET /api/historico/registro/{id} retorna 404 para ID inexistente")
    void getById_retorna404_quandoIdNaoExiste() {
        ResponseEntity<Map<String, Object>> resposta = http.exchange(
                url("/api/historico/registro/99999"),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/historico/health retorna status UP")
    void health_retornaStatusUp() {
        ResponseEntity<Map<String, Object>> resposta = http.exchange(
                url("/api/historico/health"),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).containsEntry("status", "UP");
        assertThat(resposta.getBody()).containsEntry("servico", "storage-layer");
    }

    @Test
    @DisplayName("Fluxo completo: POST salva e GET por ID recupera o mesmo registro")
    void fluxoCompleto_postSalvaEGetPorIdRecupera() {
        MetricRecord payload = MetricRecord.builder()
                .nome("throughput_rps")
                .valor(127.4)
                .unidade("req/s")
                .tags("{\"servico\":\"api-interceptor\"}")
                .fonte("observability-pipeline")
                .build();

        // POST — criar
        ResponseEntity<MetricRecord> criado = http.postForEntity(
                url("/api/historico"), payload, MetricRecord.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = criado.getBody().getId();

        // GET por ID — recuperar
        ResponseEntity<MetricRecord> recuperado = http.getForEntity(
                url("/api/historico/registro/" + id), MetricRecord.class);

        assertThat(recuperado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recuperado.getBody().getNome()).isEqualTo("throughput_rps");
        assertThat(recuperado.getBody().getValor()).isEqualTo(127.4);
        assertThat(recuperado.getBody().getTags()).contains("api-interceptor");
    }
}
