package org.aeza.aezaserver.integration;

import org.aeza.aezaserver.config.IntegrationConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WebhookClient {
    private final String webhookUrl;
    private final RestClient restClient;

    public WebhookClient(
            IntegrationConfig.WebhookSettings webhookSettings,
            RestClient.Builder restClientBuilder
    ) {
        this.webhookUrl = webhookSettings.url();
        this.restClient = restClientBuilder.build();
    }

    public void send(String eventType, String message) {
        if (webhookUrl.isBlank()) {
            return;
        }
        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", eventType, "message", message))
                .retrieve()
                .toBodilessEntity();
    }
}
