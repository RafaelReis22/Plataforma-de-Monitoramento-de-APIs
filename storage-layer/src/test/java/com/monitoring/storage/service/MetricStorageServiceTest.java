package com.monitoring.storage.service;

import com.monitoring.storage.client.PrometheusQueryClient;
import com.monitoring.storage.client.PrometheusResultado;
import com.monitoring.storage.exception.MetricNaoEncontradaException;
import com.monitoring.storage.model.MetricRecord;
import com.monitoring.storage.repository.MetricRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricStorageService — testes unitários")
class MetricStorageServiceTest {

    @Mock
    private MetricRecordRepository repository;

    @Mock
    private PrometheusQueryClient prometheusClient;

    private MetricStorageService service;

    @BeforeEach
    void setUp() {
        service = new MetricStorageService(repository, prometheusClient);
    }

    @Test
    @DisplayName("salvar deve persistir o registro e retornar o salvo com ID")
    void salvar_registroValido_persisteERetorna() {
        // Arrange
        var entrada = MetricRecord.builder()
                .nome("cpu.uso")
                .valor(72.5)
                .unidade("%")
                .fonte("monitoring-agent")
                .build();
        var esperado = MetricRecord.builder()
                .id(1L)
                .nome("cpu.uso")
                .valor(72.5)
                .unidade("%")
                .fonte("monitoring-agent")
                .timestamp(Instant.now())
                .build();
        when(repository.save(any(MetricRecord.class))).thenReturn(esperado);

        // Act
        MetricRecord resultado = service.salvar(entrada);

        // Assert
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("cpu.uso");
        verify(repository).save(any(MetricRecord.class));
    }

    @Test
    @DisplayName("salvar deve preencher timestamp automaticamente quando ausente")
    void salvar_semTimestamp_preencheAutomaticamente() {
        // Arrange
        var entrada = MetricRecord.builder()
                .nome("memoria.livre")
                .valor(2048.0)
                .unidade("MB")
                .build();
        when(repository.save(any(MetricRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        MetricRecord resultado = service.salvar(entrada);

        // Assert
        assertThat(resultado.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("buscarPorNome deve retornar lista quando registros existem")
    void buscarPorNome_metricaExiste_retornaLista() {
        // Arrange
        var registros = List.of(
                MetricRecord.builder().id(1L).nome("disco.uso").valor(55.0).build(),
                MetricRecord.builder().id(2L).nome("disco.uso").valor(58.0).build()
        );
        when(repository.findByNomeOrderByTimestampDesc("disco.uso")).thenReturn(registros);

        // Act
        List<MetricRecord> resultado = service.buscarPorNome("disco.uso");

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNome()).isEqualTo("disco.uso");
    }

    @Test
    @DisplayName("buscarPorNome deve lançar exceção quando nenhum registro existe")
    void buscarPorNome_metricaInexistente_lancaExcecao() {
        // Arrange
        when(repository.findByNomeOrderByTimestampDesc("inexistente")).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> service.buscarPorNome("inexistente"))
                .isInstanceOf(MetricNaoEncontradaException.class)
                .hasMessageContaining("inexistente");
    }

    @Test
    @DisplayName("buscarPorIntervalo deve retornar registros dentro do período")
    void buscarPorIntervalo_periodoValido_retornaRegistros() {
        // Arrange
        Instant inicio = Instant.parse("2024-01-01T00:00:00Z");
        Instant fim = Instant.parse("2024-01-01T01:00:00Z");
        var registros = List.of(
                MetricRecord.builder().nome("rede.bytes").valor(1024.0).timestamp(inicio.plusSeconds(30)).build()
        );
        when(repository.findByNomeAndTimestampBetweenOrderByTimestampAsc("rede.bytes", inicio, fim))
                .thenReturn(registros);

        // Act
        List<MetricRecord> resultado = service.buscarPorIntervalo("rede.bytes", inicio, fim);

        // Assert
        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorId deve retornar registro quando ID existe")
    void buscarPorId_idExistente_retornaRegistro() {
        // Arrange
        var registro = MetricRecord.builder().id(42L).nome("jvm.heap").valor(512.0).build();
        when(repository.findById(42L)).thenReturn(Optional.of(registro));

        // Act
        MetricRecord resultado = service.buscarPorId(42L);

        // Assert
        assertThat(resultado.getId()).isEqualTo(42L);
        assertThat(resultado.getNome()).isEqualTo("jvm.heap");
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção quando ID não existe")
    void buscarPorId_idInexistente_lancaExcecao() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(MetricNaoEncontradaException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("consultarPrometheus deve delegar ao cliente e retornar resultados")
    void consultarPrometheus_queryValida_retornaResultados() {
        // Arrange
        var resultados = List.of(
                PrometheusResultado.de(Map.of("job", "prometheus"), List.of(new double[]{1609459200.0, 1.0}))
        );
        when(prometheusClient.consultar("up")).thenReturn(resultados);

        // Act
        List<PrometheusResultado> saida = service.consultarPrometheus("up");

        // Assert
        assertThat(saida).hasSize(1);
        assertThat(saida.get(0).labels()).containsKey("job");
        verify(prometheusClient).consultar("up");
    }

    @Test
    @DisplayName("consultarPrometheusIntervalo deve delegar ao cliente com parâmetros de tempo")
    void consultarPrometheusIntervalo_parametrosValidos_delegaAoCliente() {
        // Arrange
        Instant inicio = Instant.parse("2024-01-01T00:00:00Z");
        Instant fim = Instant.parse("2024-01-01T01:00:00Z");
        when(prometheusClient.consultarIntervalo("cpu_usage_percent", inicio, fim))
                .thenReturn(List.of());

        // Act
        List<PrometheusResultado> saida = service.consultarPrometheusIntervalo("cpu_usage_percent", inicio, fim);

        // Assert
        assertThat(saida).isEmpty();
        verify(prometheusClient).consultarIntervalo("cpu_usage_percent", inicio, fim);
    }

    @Test
    @DisplayName("contarPorNome deve retornar a contagem correta do repositório")
    void contarPorNome_metricaExistente_retornaContagem() {
        // Arrange
        when(repository.countByNome("http.requests")).thenReturn(150L);

        // Act
        long contagem = service.contarPorNome("http.requests");

        // Assert
        assertThat(contagem).isEqualTo(150L);
    }
}
