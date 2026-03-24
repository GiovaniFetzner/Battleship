package com.example.battleship.dto.rest.outbound;

public class AvailableGameResponse {

    private String gameId;
    private String createdByPlayer;

    public AvailableGameResponse() {}

    public AvailableGameResponse(String gameId, String createdByPlayer) {
        this.gameId = gameId;
        this.createdByPlayer = createdByPlayer;
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getCreatedByPlayer() { return createdByPlayer; }
    public void setCreatedByPlayer(String createdByPlayer) { this.createdByPlayer = createdByPlayer; }
}

