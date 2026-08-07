package com.monitoring.dashboard.controller;

import com.monitoring.dashboard.client.PrometheusValidationClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController — testes unitários e de integração de rotas")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrometheusValidationClient validationClient;

    @Test
    @DisplayName("GET /api/dashboards deve listar os nomes dos arquivos json cadastrados")
    void listarDashboards_deveRetornarListaDeNomes() throws Exception {
        mockMvc.perform(get("/api/dashboards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasItem("hardware-overview")))
                .andExpect(jsonPath("$", hasItem("api-health")))
                .andExpect(jsonPath("$", hasItem("jvm-internals")))
                .andExpect(jsonPath("$", hasItem("slo-operacional")));
    }

    @Test
    @DisplayName("GET /api/dashboards/hardware-overview deve retornar o JSON correspondente")
    void obterDashboard_existente_deveRetornarJson() throws Exception {
        mockMvc.perform(get("/api/dashboards/hardware-overview"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Hardware Overview"));
    }

    @Test
    @DisplayName("GET /api/dashboards/inexistente deve retornar 404")
    void obterDashboard_inexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/api/dashboards/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Dashboard não encontrado"));
    }

    @Test
    @DisplayName("GET /api/dashboards/nome-invalido (caractere especial) deve retornar 400")
    void obterDashboard_nomeInvalido_deveRetornar400() throws Exception {
        mockMvc.perform(get("/api/dashboards/invalid$name"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/dashboards/validate-query com query válida deve retornar valido: true")
    void validarQuery_valida_deveRetornarSucesso() throws Exception {
        Mockito.when(validationClient.validarQuery(eq("up"), any(StringBuilder.class)))
                .thenReturn(true);

        mockMvc.perform(post("/api/dashboards/validate-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"up\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.mensagem").value("Query válida"));
    }

    @Test
    @DisplayName("POST /api/dashboards/validate-query com query inválida deve retornar valido: false com erro")
    void validarQuery_invalida_deveRetornarErro() throws Exception {
        Mockito.doAnswer(invocation -> {
            StringBuilder sb = invocation.getArgument(1);
            sb.append("parse error: double colon");
            return false;
        }).when(validationClient).validarQuery(eq("invalid::query"), any(StringBuilder.class));

        mockMvc.perform(post("/api/dashboards/validate-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"invalid::query\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensagem").value("parse error: double colon"));
    }
}
