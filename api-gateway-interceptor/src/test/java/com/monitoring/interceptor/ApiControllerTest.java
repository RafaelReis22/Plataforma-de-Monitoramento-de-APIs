package com.monitoring.interceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
@Import(ApiControllerTest.TesteConfig.class)
@DisplayName("ApiController — testes de integração com MockMvc")
class ApiControllerTest {

    @TestConfiguration
    static class TesteConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        HttpMetricsInterceptor httpMetricsInterceptor(MeterRegistry mr) {
            return new HttpMetricsInterceptor(mr);
        }

        @Bean
        WebMvcConfig webMvcConfig(HttpMetricsInterceptor interceptor) {
            return new WebMvcConfig(interceptor);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health deve retornar 200 com status UP")
    void health_deveRetornarStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.dados.status").value("UP"))
            .andExpect(jsonPath("$.dados.servico").value("api-gateway-interceptor"));
    }

    @Test
    @DisplayName("GET /api/usuarios deve retornar lista com 3 usuários")
    void listarUsuarios_deveRetornarListaComTresUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.dados").isArray())
            .andExpect(jsonPath("$.dados.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/usuarios/1 deve retornar Alice Silva")
    void buscarUsuario_idUm_deveRetornarAliceSilva() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.dados.nome").value("Alice Silva"))
            .andExpect(jsonPath("$.dados.email").value("alice@exemplo.com"));
    }

    @Test
    @DisplayName("GET /api/usuarios/999 deve retornar 404 com mensagem de erro")
    void buscarUsuario_idInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/api/usuarios/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.sucesso").value(false))
            .andExpect(jsonPath("$.erro").value("Usuário não encontrado: id=999"));
    }

    @Test
    @DisplayName("POST /api/pedidos deve retornar 201 com pedido criado")
    void criarPedido_payloadValido_deveRetornar201() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itens\": [\"produto-A\", \"produto-B\"]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sucesso").value(true))
            .andExpect(jsonPath("$.dados.status").value("CRIADO"))
            .andExpect(jsonPath("$.dados.id").value(1001));
    }

    @Test
    @DisplayName("GET /api/erro deve retornar 500 com mensagem genérica")
    void endpointComErro_deveRetornar500ComMensagemGenerica() throws Exception {
        mockMvc.perform(get("/api/erro"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.sucesso").value(false))
            .andExpect(jsonPath("$.erro").value("Erro interno do servidor"));
    }
}
