package com.example.battleship.dto.webSocket.outbound;

public class GameStartResponse extends GameEvent {

    private final String firstPlayer;

    public GameStartResponse(String gameId,
                             String firstPlayer) {

        super(GameEventType.GAME_START, gameId);
        this.firstPlayer = firstPlayer;
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }
}
