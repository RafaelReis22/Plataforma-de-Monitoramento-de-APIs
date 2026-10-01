package com.monitoring.interceptor.webhook;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${webhook.discord.url:}")
    private String discordWebhookUrl;

    @Value("${webhook.telegram.url:}")
    private String telegramWebhookUrl;

    @Value("${webhook.telegram.chat-id:}")
    private String telegramChatId;

    @Async
    @Retry(name = "webhook")
    @CircuitBreaker(name = "webhook", fallbackMethod = "fallbackAlert")
    public void sendAnomalyAlert(String method, String uri, String status, long durationMillis) {
        String message = String.format("🚨 **Anomalia Detectada!** 🚨\n- **Método:** %s\n- **URI:** %s\n- **Status:** %s\n- **Latência:** %d ms",
                method, uri, status, durationMillis);

        if (discordWebhookUrl != null && !discordWebhookUrl.isEmpty()) {
            sendDiscordAlert(message);
        }

        if (telegramWebhookUrl != null && !telegramWebhookUrl.isEmpty() && telegramChatId != null) {
            sendTelegramAlert(message);
        }
        
        if ((discordWebhookUrl == null || discordWebhookUrl.isEmpty()) && 
            (telegramWebhookUrl == null || telegramWebhookUrl.isEmpty())) {
            log.warn("Anomalia detectada, mas nenhum webhook configurado. {}", message);
        }
    }

    public void fallbackAlert(String method, String uri, String status, long durationMillis, Throwable t) {
        log.error("Circuit Breaker aberto ou falha no envio do webhook para a anomalia (URI: {}). Erro: {}", uri, t.getMessage());
    }

    private void sendDiscordAlert(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("content", message);
        restTemplate.postForEntity(discordWebhookUrl, body, String.class);
        log.info("Alerta enviado para o Discord com sucesso.");
    }

    private void sendTelegramAlert(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("chat_id", telegramChatId);
        body.put("text", message);
        restTemplate.postForEntity(telegramWebhookUrl, body, String.class);
        log.info("Alerta enviado para o Telegram com sucesso.");
    }
}
