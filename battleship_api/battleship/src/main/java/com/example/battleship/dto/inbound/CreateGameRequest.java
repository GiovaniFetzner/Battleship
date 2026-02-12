package com.example.battleship.dto.inbound;

public class CreateGameRequest extends GameMessage {

    private String playerName;

    // Default constructor for Jackson
    public CreateGameRequest() {
        this.playerName = null;
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

    @Override
    public GameMessageType getType() {
        return GameMessageType.CREATE_GAME;
    }
}