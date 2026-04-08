package com.example.battleship.config;

import com.example.battleship.controller.GameWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final String[] allowedOrigins;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler,
            @Value("${app.websocket.allowed-origins}") String allowedOriginsConfig) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.allowedOrigins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        if (this.allowedOrigins.length == 0) {
            throw new IllegalStateException("app.websocket.allowed-origins must define at least one origin");
        }
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/ws/game")
                .setAllowedOrigins(allowedOrigins);
    }
}
