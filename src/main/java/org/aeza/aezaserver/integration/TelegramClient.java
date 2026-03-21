package org.aeza.aezaserver.integration;

import org.aeza.aezaserver.config.IntegrationConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TelegramClient {
    private final String botToken;
    private final String chatId;
    private final RestClient restClient;

    public TelegramClient(
            IntegrationConfig.TelegramSettings telegramSettings,
            RestClient.Builder restClientBuilder
    ) {
        this.botToken = telegramSettings.botToken();
        this.chatId = telegramSettings.chatId();
        this.restClient = restClientBuilder.build();
    }

    public void send(String message) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return;
        }
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", message))
                .retrieve()
                .toBodilessEntity();
    }
}
