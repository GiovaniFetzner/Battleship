package com.example.battleship.dto.inbound;

public class JoinGameByCodeRequest extends GameBaseMessageRequest{
    private String roomCode;
    private String playerName;

    public JoinGameByCodeRequest() {
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
