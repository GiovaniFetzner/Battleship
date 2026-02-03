package com.example.battleship.dto.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JoinGameBaseRequest extends GameBaseMessageRequest {
    @JsonProperty("type")
    private final String type = "JOIN_GAME";

    private String playerName;

    // Default constructor for Jackson
    public JoinGameBaseRequest() {
        this.playerName = null;
    }

    public JoinGameBaseRequest(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}