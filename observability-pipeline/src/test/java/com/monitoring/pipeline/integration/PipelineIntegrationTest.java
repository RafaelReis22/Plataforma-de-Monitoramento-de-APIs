package com.monitoring.pipeline.integration;

import com.monitoring.pipeline.model.AggregatedMetric;
import com.monitoring.pipeline.model.MetricSnapshot;
import com.monitoring.pipeline.service.AggregationService;
import com.monitoring.pipeline.service.PipelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o fluxo completo de ingestão → agregação → consulta com Redis real.
 */
@Testcontainers
class PipelineIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private RedisTemplate<String, Object> redisTemplate;
    private PipelineService pipelineService;

    @BeforeEach
    void setUp() {
        var factory = new LettuceConnectionFactory(
                redis.getHost(),
                redis.getMappedPort(6379)
        );
        factory.afterPropertiesSet();

        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        var jsonSerializer  = new GenericJackson2JsonRedisSerializer(mapper);
        var stringSerializer = new StringRedisSerializer();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();

        pipelineService = new PipelineService(redisTemplate, new AggregationService());
    }

    @AfterEach
    void tearDown() {
        // Limpa chaves do teste para isolamento
        var chaves = redisTemplate.keys("metric:*");
        if (chaves != null && !chaves.isEmpty()) {
            redisTemplate.delete(chaves);
        }
    }

    @Test
    @DisplayName("armazena e recupera snapshot bruto do Redis")
    void armazenarEObterSnapshot_fluxoCompleto() {
        var snapshot = new MetricSnapshot("cpu.uso", 45.5, "%", Instant.now(), Map.of("host", "server-01"));

        pipelineService.armazenarSnapshot("cpu.uso:server-01", snapshot);
        MetricSnapshot recuperado = pipelineService.obterSnapshot("cpu.uso:server-01");

        assertThat(recuperado).isNotNull();
        assertThat(recuperado.nome()).isEqualTo("cpu.uso");
        assertThat(recuperado.valor()).isEqualTo(45.5);
        assertThat(recuperado.tags()).containsEntry("host", "server-01");
    }

    @Test
    @DisplayName("retorna null para snapshot inexistente ou expirado")
    void obterSnapshot_chaveInexistente_retornaNull() {
        MetricSnapshot resultado = pipelineService.obterSnapshot("metrica.inexistente");
        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("ciclo de agregação processa snapshots e persiste agregados no Redis")
    void executarCicloAgregacao_comSnapshots_persisteAgregados() {
        pipelineService.armazenarSnapshot("cpu.uso:s1", new MetricSnapshot("cpu.uso", 10.0, "%", Instant.now(), Map.of()));
        pipelineService.armazenarSnapshot("cpu.uso:s2", new MetricSnapshot("cpu.uso", 30.0, "%", Instant.now(), Map.of()));
        pipelineService.armazenarSnapshot("ram.uso:s1", new MetricSnapshot("ram.uso", 60.0, "%", Instant.now(), Map.of()));

        pipelineService.executarCicloAgregacao();

        List<AggregatedMetric> agregados = pipelineService.listarAgregados();
        assertThat(agregados).hasSizeGreaterThanOrEqualTo(2);

        var cpuOpt = agregados.stream().filter(a -> a.nome().equals("cpu.uso")).findFirst();
        assertThat(cpuOpt).isPresent();
        assertThat(cpuOpt.get().count()).isEqualTo(2);
        assertThat(cpuOpt.get().min()).isEqualTo(10.0);
        assertThat(cpuOpt.get().max()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("ciclo de agregação sem snapshots não gera erros nem agregados")
    void executarCicloAgregacao_semSnapshots_semEfeito() {
        pipelineService.executarCicloAgregacao();
        List<AggregatedMetric> agregados = pipelineService.listarAgregados();
        assertThat(agregados).isEmpty();
    }
}
