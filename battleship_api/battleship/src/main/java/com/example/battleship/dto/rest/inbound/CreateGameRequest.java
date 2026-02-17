package com.example.battleship.dto.rest.inbound;

public class CreateGameRequest {

    private String playerName;

    public CreateGameRequest() {
    }

    public CreateGameRequest(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}