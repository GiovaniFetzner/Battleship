package com.example.battleship.dto.webSocket.outbound;

public class PlayerReadyResponse extends GameEvent {

    private final String playerId;
    private final boolean bothReady;

    public PlayerReadyResponse(String gameId,
                               String playerId,
                               boolean bothReady) {

        super(GameEventType.PLAYER_READY, gameId);
        this.playerId = playerId;
        this.bothReady = bothReady;
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isBothReady() {
        return bothReady;
    }
}

