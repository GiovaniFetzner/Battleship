package com.example.battleship.dto.inbound;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("JOIN_GAME")
public class JoinGameBaseRequest extends GameBaseMessageRequest {
    private String playerName;

    public JoinGameBaseRequest() {
    }

    public JoinGameBaseRequest(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String getType() {
        return "JOIN_GAME";
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}