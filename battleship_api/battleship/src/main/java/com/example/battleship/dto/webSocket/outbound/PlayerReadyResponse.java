package com.example.battleship.dto.webSocket.outbound;

public class PlayerReadyResponse extends GameEvent {

    private final String playerName;
    private final boolean bothReady;

    public PlayerReadyResponse(String gameId,
                               String playerId,
                               boolean bothReady) {

        super(GameEventType.PLAYER_READY, gameId);
        this.playerName = playerId;
        this.bothReady = bothReady;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isBothReady() {
        return bothReady;
    }
}

