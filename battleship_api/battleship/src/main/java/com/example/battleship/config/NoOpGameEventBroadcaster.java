package com.example.battleship.config;

import java.util.Collection;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import com.example.battleship.webSocket.GameEventBroadcaster;

@Component
@Profile("dev")
public class NoOpGameEventBroadcaster implements GameEventBroadcaster {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NoOpGameEventBroadcaster.class);

    @Override
    public void broadcast(String gameId, GameEvent event) {
        log.debug("[dev] broadcast ignorado — gameId={}, event={}", gameId, event);
    }

    @Override
    public void broadcastToPlayers(String gameId, Collection<String> playerNames, GameEvent event) {
        log.debug("[dev] broadcastToPlayers ignorado — gameId={}", gameId);
    }

    @Override
    public void sendToPlayer(String gameId, String playerName, GameEvent event) {
        log.debug("[dev] sendToPlayer ignorado — gameId={}, player={}", gameId, playerName);
    }

    @Override
    public void register(String gameId, String playerName, WebSocketSession session) {
        // no-op
    }

    @Override
    public void removeSession(WebSocketSession session) {
        // no-op
    }

    @Override
    public void updateActivityTime(String gameId, String playerName) {
        // no-op
    }
}