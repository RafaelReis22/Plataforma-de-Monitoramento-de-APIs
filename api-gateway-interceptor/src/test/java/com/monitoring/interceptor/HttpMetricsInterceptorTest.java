package com.monitoring.interceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpMetricsInterceptor — testes unitários")
class HttpMetricsInterceptorTest {

    private MeterRegistry meterRegistry;
    private HttpMetricsInterceptor interceptor;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        interceptor = new HttpMetricsInterceptor(meterRegistry);
    }

    @Test
    @DisplayName("preHandle deve registrar startTime na requisição e retornar true")
    void preHandle_deveRegistrarStartTimeERetornarTrue() {
        var request  = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        boolean resultado = interceptor.preHandle(request, response, new Object());

        assertThat(resultado).isTrue();
        assertThat(request.getAttribute("request.startTime")).isNotNull().isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("afterCompletion deve registrar timer http.server.requests para GET 200")
    void afterCompletion_deveRegistrarTimerParaGet200() {
        var request  = new MockHttpServletRequest("GET", "/api/usuarios");
        var response = new MockHttpServletResponse();
        response.setStatus(200);
        request.setAttribute("request.startTime", System.nanoTime() - 1_000_000L);

        interceptor.afterCompletion(request, response, new Object(), null);

        var timer = meterRegistry.find("http.server.requests")
            .tag("method", "GET")
            .tag("uri", "/api/usuarios")
            .tag("status", "200")
            .tag("outcome", "SUCCESS")
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("afterCompletion deve incrementar contador de erros em resposta 5xx")
    void afterCompletion_status500_deveIncrementarContadorDeErros() {
        var request  = new MockHttpServletRequest("GET", "/api/erro");
        var response = new MockHttpServletResponse();
        response.setStatus(500);
        request.setAttribute("request.startTime", System.nanoTime() - 1_000_000L);

        interceptor.afterCompletion(request, response, new Object(), new RuntimeException("falha"));

        var counter = meterRegistry.find("http.server.errors.total")
            .tag("method", "GET")
            .tag("uri", "/api/erro")
            .tag("status", "500")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("afterCompletion sem startTime não deve lançar exceção")
    void afterCompletion_semStartTime_deveIgnorarSilenciosamente() {
        var request  = new MockHttpServletRequest("GET", "/api/health");
        var response = new MockHttpServletResponse();
        response.setStatus(200);
        // sem startTime no atributo

        interceptor.afterCompletion(request, response, new Object(), null);
        // nenhuma métrica deve ter sido registrada
        assertThat(meterRegistry.find("http.server.requests").timer()).isNull();
    }

    @Test
    @DisplayName("sanitizeUri deve substituir IDs numéricos por {id}")
    void afterCompletion_uriComId_deveSanitizarParaPlaceholder() {
        var request  = new MockHttpServletRequest("GET", "/api/usuarios/42");
        var response = new MockHttpServletResponse();
        response.setStatus(200);
        request.setAttribute("request.startTime", System.nanoTime() - 1_000_000L);

        interceptor.afterCompletion(request, response, new Object(), null);

        var timer = meterRegistry.find("http.server.requests")
            .tag("uri", "/api/usuarios/{id}")
            .timer();
        assertThat(timer).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
        "200, SUCCESS",
        "301, REDIRECTION",
        "404, CLIENT_ERROR",
        "500, SERVER_ERROR",
        "999, UNKNOWN"
    })
    @DisplayName("outcome deve ser resolvido corretamente conforme o status HTTP")
    void afterCompletion_deveResolverOutcomeCorreto(int status, String outcomeEsperado) {
        var request  = new MockHttpServletRequest("GET", "/api/test");
        var response = new MockHttpServletResponse();
        response.setStatus(status);
        request.setAttribute("request.startTime", System.nanoTime() - 1_000_000L);

        interceptor.afterCompletion(request, response, new Object(), null);

        var timer = meterRegistry.find("http.server.requests")
            .tag("outcome", outcomeEsperado)
            .timer();
        assertThat(timer).isNotNull();
    }
}
