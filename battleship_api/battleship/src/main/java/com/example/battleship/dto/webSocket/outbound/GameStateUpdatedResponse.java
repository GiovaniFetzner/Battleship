package com.example.battleship.dto.webSocket.outbound;

public class GameStateUpdatedResponse extends GameEvent {

    private final String joinedPlayerName;

    public GameStateUpdatedResponse(String gameId, String joinedPlayerName) {
        super(GameEventType.GAME_STATE_UPDATED, gameId);
        this.joinedPlayerName = joinedPlayerName;
    }

    public String getJoinedPlayerName() {
        return joinedPlayerName;
    }
}
