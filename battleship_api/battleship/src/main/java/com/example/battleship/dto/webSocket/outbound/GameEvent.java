package com.example.battleship.dto.webSocket.outbound;

public abstract class GameEvent {

    private final GameEventType type;
    private final String gameId;

    protected GameEvent(GameEventType type, String gameId) {
        this.type = type;
        this.gameId = gameId;
    }

    public GameEventType getType() {
        return type;
    }

    public String getGameId() {
        return gameId;
    }
}

