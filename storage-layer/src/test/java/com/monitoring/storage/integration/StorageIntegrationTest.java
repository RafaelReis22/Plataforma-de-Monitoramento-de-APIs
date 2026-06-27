package com.monitoring.storage.integration;

import com.monitoring.storage.client.PrometheusQueryClient;
import com.monitoring.storage.exception.MetricNaoEncontradaException;
import com.monitoring.storage.model.MetricRecord;
import com.monitoring.storage.service.MetricStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

// Sobe o contexto Spring Boot completo com PostgreSQL real via Testcontainers
@SpringBootTest
@Testcontainers
@DisplayName("StorageLayer — testes de integração com PostgreSQL real")
class StorageIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("monitoring_test")
            .withUsername("test")
            .withPassword("test");

    // Substitui o datasource pelo container dinâmico antes do contexto Spring subir
    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MetricStorageService service;

    // Mock do cliente Prometheus — não deve fazer chamadas HTTP nos testes de integração JPA
    @MockBean
    private PrometheusQueryClient prometheusClient;

    @AfterEach
    void limpar(@Autowired com.monitoring.storage.repository.MetricRecordRepository repo) {
        repo.deleteAll();
    }

    @Test
    @DisplayName("salvar e buscarPorNome retorna o registro persistido no PostgreSQL")
    void salvarEBuscarPorNome_registroValido_persisteNoBanco() {
        // Arrange
        var registro = MetricRecord.builder()
                .nome("cpu.uso")
                .valor(65.3)
                .unidade("%")
                .timestamp(Instant.now())
                .fonte("monitoring-agent")
                .build();

        // Act
        service.salvar(registro);
        List<MetricRecord> resultado = service.buscarPorNome("cpu.uso");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("cpu.uso");
        assertThat(resultado.get(0).getValor()).isEqualTo(65.3);
        assertThat(resultado.get(0).getId()).isNotNull();
    }

    @Test
    @DisplayName("buscarPorNome lança exceção quando métrica não existe no banco")
    void buscarPorNome_metricaAusente_lancaExcecao() {
        assertThatThrownBy(() -> service.buscarPorNome("metrica.inexistente"))
                .isInstanceOf(MetricNaoEncontradaException.class)
                .hasMessageContaining("metrica.inexistente");
    }

    @Test
    @DisplayName("buscarPorIntervalo filtra registros corretamente pelo período de tempo")
    void buscarPorIntervalo_comMultiplosRegistros_filtraPorPeriodo() {
        // Arrange
        Instant base = Instant.parse("2024-06-01T12:00:00Z");
        service.salvar(MetricRecord.builder().nome("rede.bytes").valor(100.0).timestamp(base.minusSeconds(300)).build());
        service.salvar(MetricRecord.builder().nome("rede.bytes").valor(200.0).timestamp(base).build());
        service.salvar(MetricRecord.builder().nome("rede.bytes").valor(300.0).timestamp(base.plusSeconds(300)).build());

        // Act — janela estreita: apenas o registro no instante `base` entra
        List<MetricRecord> resultado = service.buscarPorIntervalo(
                "rede.bytes",
                base.minusSeconds(60),
                base.plusSeconds(60)
        );

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getValor()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("buscarPorId retorna o registro específico após múltiplas inserções")
    void buscarPorId_registroExistente_retornaEspecifico() {
        // Arrange
        var r1 = service.salvar(MetricRecord.builder().nome("jvm.heap").valor(512.0).timestamp(Instant.now()).build());
        service.salvar(MetricRecord.builder().nome("jvm.heap").valor(768.0).timestamp(Instant.now()).build());

        // Act
        MetricRecord encontrado = service.buscarPorId(r1.getId());

        // Assert
        assertThat(encontrado.getId()).isEqualTo(r1.getId());
        assertThat(encontrado.getValor()).isEqualTo(512.0);
    }

    @Test
    @DisplayName("contarPorNome retorna a contagem correta após várias inserções")
    void contarPorNome_aposInsercoesMultiplas_retornaContagemCorreta() {
        // Arrange — insere 5 registros para "disco.io" e 1 para outra métrica
        for (int i = 0; i < 5; i++) {
            service.salvar(MetricRecord.builder()
                    .nome("disco.io")
                    .valor((double) i * 10)
                    .timestamp(Instant.now().plusSeconds(i))
                    .build());
        }
        service.salvar(MetricRecord.builder().nome("outra.metrica").valor(1.0).timestamp(Instant.now()).build());

        // Act
        long contagem = service.contarPorNome("disco.io");

        // Assert
        assertThat(contagem).isEqualTo(5L);
    }
}
