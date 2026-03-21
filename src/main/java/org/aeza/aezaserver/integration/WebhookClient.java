package org.aeza.aezaserver.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WebhookClient {
    private final String webhookUrl;
    private final RestClient restClient;

    public WebhookClient(@Value("${integration.webhook.url:}") String webhookUrl, RestClient.Builder restClientBuilder) {
        this.webhookUrl = webhookUrl;
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
