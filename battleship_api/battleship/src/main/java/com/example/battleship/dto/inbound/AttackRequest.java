package com.example.battleship.dto.inbound;

public class AttackRequest extends GameMessage {

    private String gameId;
    private String playerId;
    private int x;
    private int y;

    public AttackRequest() {
    }

    public AttackRequest(String gameId, String playerId, int x, int y) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.ATTACK;
    }
}