package com.monitoring.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final List<Map<String, Object>> USUARIOS = List.of(
        Map.of("id", 1, "nome", "Alice Silva",  "email", "alice@exemplo.com"),
        Map.of("id", 2, "nome", "Bruno Costa",  "email", "bruno@exemplo.com"),
        Map.of("id", 3, "nome", "Carla Souza",  "email", "carla@exemplo.com")
    );

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP", "servico", "api-gateway-interceptor"));
    }

    @GetMapping("/usuarios")
    public ApiResponse<List<Map<String, Object>>> listarUsuarios() {
        log.info("Listando {} usuário(s)", USUARIOS.size());
        return ApiResponse.ok(USUARIOS);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buscarUsuario(@PathVariable("id") int id) {
        return USUARIOS.stream()
            .filter(u -> u.get("id").equals(id))
            .findFirst()
            .map(u -> ResponseEntity.ok(ApiResponse.<Map<String, Object>>ok(u)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.erro("Usuário não encontrado: id=" + id)));
    }

    @PostMapping("/pedidos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> criarPedido(
            @RequestBody Map<String, Object> payload) {
        log.info("Criando pedido com payload: {}", payload);
        var pedido = Map.<String, Object>of(
            "id",     1001,
            "status", "CRIADO",
            "itens",  payload.getOrDefault("itens", List.of())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(pedido));
    }

    @GetMapping("/lento")
    public ApiResponse<Map<String, String>> endpointLento() throws InterruptedException {
        // Simula latência elevada para exercitar métricas de histograma
        Thread.sleep(500);
        return ApiResponse.ok(Map.of("mensagem", "Resposta lenta processada com sucesso"));
    }

    @GetMapping("/erro")
    public ApiResponse<Void> endpointComErro() {
        throw new RuntimeException("Erro simulado para testes de monitoramento");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Erro não tratado na requisição: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.erro("Erro interno do servidor"));
    }
}
