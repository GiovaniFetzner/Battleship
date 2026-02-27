package com.example.battleship.webSocket;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameEventBroadcasterImpl implements GameEventBroadcaster {

    private final ObjectMapper objectMapper;

    // gameId → (playerId → session)
    private final Map<String, Map<String, WebSocketSession>> sessions =
            new ConcurrentHashMap<>();

    public GameEventBroadcasterImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(String gameId, String playerName, WebSocketSession session) {
        sessions
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, session);
    }

    @Override
    public void broadcast(String gameId, GameEvent event) {

        Map<String, WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions == null) return;

        String json = serialize(event);

        for (WebSocketSession ws : gameSessions.values()) {
            send(ws, json);
        }

        System.out.println("Broadcast para gameId=" + gameId);
        System.out.println("Sessions registradas: " + sessions);

    }

    @Override
    public void sendToPlayer(String gameId, String playerName, GameEvent event) {

        Map<String, WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions == null) return;

        WebSocketSession session = gameSessions.get(playerName);
        if (session == null) return;

        String json = serialize(event);
        send(session, json);
    }

    @Override
    public void removeSession(WebSocketSession session) {

        sessions.values().forEach(playerMap ->
                playerMap.values().removeIf(ws ->
                        ws.getId().equals(session.getId()))
        );
    }

    // ------------------------
    // Helpers
    // ------------------------

    private String serialize(GameEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize GameEvent", e);
        }
    }

    private void send(WebSocketSession session, String json) {

        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            removeSession(session);
        }
    }
}
