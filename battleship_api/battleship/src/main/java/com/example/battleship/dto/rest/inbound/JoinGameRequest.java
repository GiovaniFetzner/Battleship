package com.example.battleship.dto.rest.inbound;

public class JoinGameRequest {

    private String gameId;
    private String playerName;

    public JoinGameRequest() {
    }

    public JoinGameRequest(String gameId, String playerName) {
        this.gameId = gameId;
        this.playerName = playerName;
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
}
