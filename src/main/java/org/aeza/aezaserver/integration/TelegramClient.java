package org.aeza.aezaserver.integration;

import org.aeza.aezaserver.config.IntegrationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TelegramClient {
    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);

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
            log.warn("Telegram alert skipped: bot token or chat id is empty");
            return;
        }
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", message))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Telegram alert sent to chat {} with status {}", chatId, response.getStatusCode().value());
        } catch (RuntimeException ex) {
            log.error("Telegram alert send failed: {}", ex.getMessage());
        }
    }
}
