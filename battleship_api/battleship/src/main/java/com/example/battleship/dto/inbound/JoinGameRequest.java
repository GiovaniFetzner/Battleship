package com.example.battleship.dto.inbound;

public class JoinGameRequest extends GameMessage {
    private String roomCode;
    private String playerName;

    public JoinGameRequest() {
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

    @Override
    public GameMessageType getType() {
        return GameMessageType.JOIN_GAME;
    }
}
