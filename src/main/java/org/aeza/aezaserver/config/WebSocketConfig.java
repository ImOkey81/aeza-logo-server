package org.aeza.aezaserver.config;

import org.aeza.aezaserver.service.LiveWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final LiveWebSocketHandler liveWebSocketHandler;

    public WebSocketConfig(LiveWebSocketHandler liveWebSocketHandler) {
        this.liveWebSocketHandler = liveWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveWebSocketHandler, "/api/v1/live/ws")
                .setAllowedOriginPatterns("*");
    }
}
