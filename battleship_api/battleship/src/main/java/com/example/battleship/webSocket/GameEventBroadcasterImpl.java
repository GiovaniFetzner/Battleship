package com.example.battleship.webSocket;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class GameEventBroadcasterImpl implements GameEventBroadcaster {

    private final ObjectMapper objectMapper;
    private static final long INACTIVITY_TIMEOUT_MINUTES = 5;
    private static final long CHECK_INTERVAL_MINUTES = 1;

    // gameId → (playerId → session)
    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    // gameId → (playerId → lastActivityTime)
    private final Map<String, Map<String, Long>> lastActivityTime = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;

    public GameEventBroadcasterImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startInactivityMonitor();
    }

    private void startInactivityMonitor() {
        scheduler.scheduleAtFixedRate(
                this::checkInactiveSessions,
                CHECK_INTERVAL_MINUTES,
                CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    private void checkInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = INACTIVITY_TIMEOUT_MINUTES * 60 * 1000;

        sessions.forEach((gameId, playerSessions) -> {
            Map<String, Long> activityMap = lastActivityTime.get(gameId);
            if (activityMap == null)
                return;

            playerSessions.forEach((playerName, session) -> {
                Long lastActivity = activityMap.get(playerName);
                if (lastActivity == null)
                    return;

                long inactivityDuration = currentTime - lastActivity;
                if (inactivityDuration > timeoutMillis) {
                    closeInactiveSession(gameId, playerName, session);
                }
            });
        });
    }

    private void closeInactiveSession(String gameId, String playerName, WebSocketSession session) {
        try {
            session.close(CloseStatus.SESSION_NOT_RELIABLE);
            removeSession(session);
            System.out.println("Sessão inativa fechada - GameID: " + gameId + ", Player: " + playerName);
        } catch (IOException e) {
            System.err.println("Erro ao fechar sessão inativa: " + e.getMessage());
        }
    }

    @Override
    public void register(String gameId, String playerName, WebSocketSession session) {
        sessions
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, session);

        lastActivityTime
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, System.currentTimeMillis());
    }

    @Override
    public void broadcast(String gameId, GameEvent event) {

        Map<String, WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions == null)
            return;

        String json = serialize(event);

        for (WebSocketSession ws : gameSessions.values()) {
            send(ws, json);
        }

        System.out.println("Broadcast para gameId=" + gameId);
        System.out.println("Sessions registradas: " + sessions);

    }

    public void sendToPlayer(String gameId, String playerName, GameEvent event) {

        Map<String, WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions == null)
            return;

        WebSocketSession session = gameSessions.get(playerName);
        if (session == null)
            return;

        String json = serialize(event);
        send(session, json);
    }

    @Override
    public void removeSession(WebSocketSession session) {

        sessions.values().forEach(playerMap -> playerMap.values().removeIf(ws -> ws.getId().equals(session.getId())));

        lastActivityTime.values().forEach(activityMap -> activityMap.values().removeIf(ignored -> true));
    }

    public void updateActivityTime(String gameId, String playerName) {
        lastActivityTime
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, System.currentTimeMillis());
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
