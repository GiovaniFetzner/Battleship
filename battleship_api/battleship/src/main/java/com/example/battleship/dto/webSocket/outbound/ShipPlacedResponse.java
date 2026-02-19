package com.example.battleship.dto.webSocket.outbound;

public class ShipPlacedResponse extends GameEvent {

    private final String playerName;

    public ShipPlacedResponse(String gameId,
                              String playerId) {

        super(GameEventType.SHIP_PLACED, gameId);
        this.playerName = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
