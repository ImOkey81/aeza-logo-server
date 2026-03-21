package org.aeza.aezaserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntegrationConfig {

    @Bean
    public TelegramSettings telegramSettings() {
        return new TelegramSettings(
                "8746703923:AAGGwrf0o3Mn8XOzf3thQqfd8U4cqwkPwr0",
                "-1003869066185"
        );
    }

    @Bean
    public WebhookSettings webhookSettings() {
        return new WebhookSettings("");
    }

    public record TelegramSettings(String botToken, String chatId) {
    }

    public record WebhookSettings(String url) {
    }
}
