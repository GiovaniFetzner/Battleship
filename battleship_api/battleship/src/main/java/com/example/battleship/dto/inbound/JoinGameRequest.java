package com.example.battleship.dto.inbound;

public class JoinGameRequest extends GameMessage {
    private String gameId;
    private String playerName;

    public JoinGameRequest() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.JOIN_GAME;
    }
}
