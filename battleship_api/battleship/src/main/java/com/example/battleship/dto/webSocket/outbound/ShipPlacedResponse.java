package com.example.battleship.dto.webSocket.outbound;

public class ShipPlacedResponse extends GameEvent {

    private final String playerId;

    public ShipPlacedResponse(String gameId,
                              String playerId) {

        super(GameEventType.SHIP_PLACED, gameId);
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}
