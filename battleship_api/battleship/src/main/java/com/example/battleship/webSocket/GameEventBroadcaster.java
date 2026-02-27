package com.example.battleship.webSocket;

import com.example.battleship.dto.webSocket.outbound.GameEvent;
import org.springframework.web.socket.WebSocketSession;

public interface GameEventBroadcaster {

    void register(String gameId, String playerName, WebSocketSession session);

    void broadcast(String gameId, GameEvent event);

    void sendToPlayer(String gameId, String playerName, GameEvent event);

    void removeSession(WebSocketSession session);
}

