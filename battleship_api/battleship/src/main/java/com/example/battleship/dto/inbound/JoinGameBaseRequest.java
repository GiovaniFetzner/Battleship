package com.example.battleship.dto.inbound;

public class JoinGameBaseRequest extends GameBaseMessageRequest {

    private String playerName;

    // Default constructor for Jackson
    public JoinGameBaseRequest() {
        this.playerName = null;
    }

    public JoinGameBaseRequest(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}