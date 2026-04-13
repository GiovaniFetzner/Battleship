package com.example.battleship.webSocket;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class GameEventBroadcasterImpl implements GameEventBroadcaster {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer redisListenerContainer;
    private final MessageListener redisMessageListener;
    private static final long INACTIVITY_TIMEOUT_MINUTES = 5;
    private static final long CHECK_INTERVAL_MINUTES = 1;
    private static final String PLAYER_CHANNEL_PREFIX = "ws:game:";
    private static final String PLAYER_CHANNEL_SEPARATOR = ":player:";

    // gameId → (playerId → session)
    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    // gameId → (playerId → lastActivityTime)
    private final Map<String, Map<String, Long>> lastActivityTime = new ConcurrentHashMap<>();

    // Channel -> number of local sessions using this channel
    private final Map<String, Integer> channelSubscriptionCount = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;

    public GameEventBroadcasterImpl(ObjectMapper objectMapper,
            StringRedisTemplate redisTemplate,
            RedisMessageListenerContainer redisListenerContainer) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.redisListenerContainer = redisListenerContainer;
        this.redisMessageListener = this::onRedisMessage;
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
        Map<String, WebSocketSession> gameSessions = sessions.computeIfAbsent(gameId, g -> new ConcurrentHashMap<>());
        boolean isNewRegistration = gameSessions.put(playerName, session) == null;

        lastActivityTime
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, System.currentTimeMillis());

        if (isNewRegistration) {
            // Subscribe to broadcast channel for this game so we receive events for all
            // players
            subscribeToPlayerChannel(gameId, playerName);
        }
    }

    @Override
    public void broadcast(String gameId, GameEvent event) {

        Map<String, WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions == null)
            return;

        broadcastToPlayers(gameId, gameSessions.keySet(), event);

        System.out.println("Broadcast para gameId=" + gameId);
        System.out.println("Sessions registradas: " + sessions);

    }

    @Override
    public void broadcastToPlayers(String gameId, Collection<String> playerNames, GameEvent event) {

        if (playerNames == null || playerNames.isEmpty()) {
            return;
        }

        String json = serialize(event);

        for (String playerName : new HashSet<>(playerNames)) {
            if (playerName == null || playerName.isBlank()) {
                continue;
            }

            publishToPlayerChannel(gameId, playerName, json);
        }
    }

    public void sendToPlayer(String gameId, String playerName, GameEvent event) {

        String json = serialize(event);
        publishToPlayerChannel(gameId, playerName, json);
    }

    @Override
    public void removeSession(WebSocketSession session) {

        sessions.forEach((gameId, playerMap) -> {
            playerMap.forEach((playerName, ws) -> {
                if (!ws.getId().equals(session.getId())) {
                    return;
                }

                playerMap.remove(playerName);
                Map<String, Long> activityMap = lastActivityTime.get(gameId);
                if (activityMap != null) {
                    activityMap.remove(playerName);
                    if (activityMap.isEmpty()) {
                        lastActivityTime.remove(gameId);
                    }
                }

                unsubscribeFromPlayerChannelIfUnused(gameId, playerName);
            });

            if (playerMap.isEmpty()) {
                sessions.remove(gameId);
            }
        });
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

    private void publishToPlayerChannel(String gameId, String playerName, String json) {
        String channel = playerChannel(gameId, playerName);
        redisTemplate.convertAndSend(channel, json);
    }

    private void subscribeToPlayerChannel(String gameId, String playerName) {
        String channel = playerChannel(gameId, playerName);

        channelSubscriptionCount.compute(channel, (ignored, currentCount) -> {
            if (currentCount == null || currentCount == 0) {
                redisListenerContainer.addMessageListener(redisMessageListener, new ChannelTopic(channel));
                return 1;
            }

            return currentCount + 1;
        });
    }

    private void unsubscribeFromPlayerChannelIfUnused(String gameId, String playerName) {
        if (hasLocalSession(gameId, playerName)) {
            return;
        }

        String channel = playerChannel(gameId, playerName);

        channelSubscriptionCount.computeIfPresent(channel, (ignored, currentCount) -> {
            if (currentCount <= 1) {
                redisListenerContainer.removeMessageListener(redisMessageListener, new ChannelTopic(channel));
                return null;
            }

            return currentCount - 1;
        });
    }

    private boolean hasLocalSession(String gameId, String playerName) {
        Map<String, WebSocketSession> playerMap = sessions.get(gameId);
        return playerMap != null && playerMap.containsKey(playerName);
    }

    private void onRedisMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        ChannelTarget target = parsePlayerChannel(channel);
        if (target == null) {
            return;
        }

        Map<String, WebSocketSession> gameSessions = sessions.get(target.gameId());
        if (gameSessions == null) {
            return;
        }

        WebSocketSession session = gameSessions.get(target.playerName());
        send(session, json);
    }

    private ChannelTarget parsePlayerChannel(String channel) {
        if (!channel.startsWith(PLAYER_CHANNEL_PREFIX)) {
            return null;
        }

        int separatorIdx = channel.indexOf(PLAYER_CHANNEL_SEPARATOR, PLAYER_CHANNEL_PREFIX.length());
        if (separatorIdx < 0) {
            return null;
        }

        int gameIdStart = PLAYER_CHANNEL_PREFIX.length();
        int playerStart = separatorIdx + PLAYER_CHANNEL_SEPARATOR.length();

        if (playerStart >= channel.length()) {
            return null;
        }

        String gameId = channel.substring(gameIdStart, separatorIdx);
        String playerName = channel.substring(playerStart);

        return new ChannelTarget(gameId, playerName);
    }

    private String playerChannel(String gameId, String playerName) {
        return PLAYER_CHANNEL_PREFIX + gameId + PLAYER_CHANNEL_SEPARATOR + playerName;
    }

    private record ChannelTarget(String gameId, String playerName) {
    }
}
