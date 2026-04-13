package com.example.battleship.webSocket;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

public interface GameEventBroadcaster {

    void register(String gameId, String playerName, WebSocketSession session);

    void broadcast(String gameId, GameEvent event);

    void broadcastToPlayers(String gameId, Collection<String> playerNames, GameEvent event);

    void sendToPlayer(String gameId, String playerName, GameEvent event);

    void removeSession(WebSocketSession session);

    void updateActivityTime(String gameId, String playerName);
}
