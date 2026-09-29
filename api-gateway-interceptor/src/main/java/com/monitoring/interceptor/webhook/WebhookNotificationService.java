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

    @Value("${webhook.slack.url:}")
    private String slackWebhookUrl;

    @Async
    public void sendAnomalyAlert(String method, String uri, String status, long durationMillis) {
        String message = String.format("🚨 **Anomalia Detectada!** 🚨\n- **Método:** %s\n- **URI:** %s\n- **Status:** %s\n- **Latência:** %d ms",
                method, uri, status, durationMillis);

        if (discordWebhookUrl != null && !discordWebhookUrl.isEmpty()) {
            sendDiscordAlert(message);
        }

        if (slackWebhookUrl != null && !slackWebhookUrl.isEmpty()) {
            sendSlackAlert(message);
        }
        
        if ((discordWebhookUrl == null || discordWebhookUrl.isEmpty()) && 
            (slackWebhookUrl == null || slackWebhookUrl.isEmpty())) {
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

    private void sendSlackAlert(String message) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", message);
            restTemplate.postForEntity(slackWebhookUrl, body, String.class);
            log.info("Alerta enviado para o Slack com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao enviar alerta para o Slack", e);
        }
    }
}
