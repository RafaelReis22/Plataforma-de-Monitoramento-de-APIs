package com.monitoring.interceptor.webhook;

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

    private void sendDiscordAlert(String message) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("content", message);
            restTemplate.postForEntity(discordWebhookUrl, body, String.class);
            log.info("Alerta enviado para o Discord com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao enviar alerta para o Discord", e);
        }
    }

    private void sendTelegramAlert(String message) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("chat_id", telegramChatId);
            body.put("text", message);
            restTemplate.postForEntity(telegramWebhookUrl, body, String.class);
            log.info("Alerta enviado para o Telegram com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao enviar alerta para o Telegram", e);
        }
    }
}
