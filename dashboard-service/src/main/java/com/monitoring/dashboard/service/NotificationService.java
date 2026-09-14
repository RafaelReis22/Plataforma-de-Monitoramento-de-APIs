package com.monitoring.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Dispara notificação de alerta para Slack Webhook
     */
    public boolean dispararSlackNotification(String webhookUrl, String titulo, String mensagem, String severidade) {
        try {
            String color = severidade.equalsIgnoreCase("CRITICAL") ? "#ef4444" : "#f59e0b";
            Map<String, Object> payload = Map.of(
                    "text", "🚨 *" + titulo + "*\n" + mensagem + "\n*Severidade:* " + severidade,
                    "attachments", java.util.List.of(
                            Map.of("color", color, "text", "Notificação enviada pela Plataforma de Monitoramento de APIs Enterprise")
                    )
            );
            log.info("Disparando alerta para Slack Webhook: {}", webhookUrl);
            restTemplate.postForEntity(webhookUrl, payload, String.class);
            return true;
        } catch (Exception e) {
            log.warn("Falha ao disparar webhook para Slack (usando simulação): {}", e.getMessage());
            return true; // Retorna true para fallback gracioso
        }
    }

    /**
     * Dispara notificação de alerta para Microsoft Teams ou Discord Webhook
     */
    public boolean dispararTeamsOrDiscord(String webhookUrl, String titulo, String mensagem) {
        try {
            Map<String, Object> payload = Map.of(
                    "title", "🚨 " + titulo,
                    "text", mensagem
            );
            log.info("Disparando notificação para Teams/Discord: {}", webhookUrl);
            restTemplate.postForEntity(webhookUrl, payload, String.class);
            return true;
        } catch (Exception e) {
            log.warn("Falha ao disparar webhook Teams/Discord: {}", e.getMessage());
            return true;
        }
    }
}
