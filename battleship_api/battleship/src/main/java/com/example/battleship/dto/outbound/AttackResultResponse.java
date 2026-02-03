package com.example.battleship.dto.outbound;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ATTACK_RESULT")
public class AttackResultResponse extends GameBaseMessageResponse {
    private String result; // "HIT", "MISS", "DESTROYED"
    private int x;
    private int y;
    private String currentPlayer;
    private boolean gameOver;
    private String winner;
    private String destroyedShip;

    public AttackResultResponse() {}

    public AttackResultResponse(String result, int x, int y, String currentPlayer) {
        this.result = result;
        this.x = x;
        this.y = y;
        this.currentPlayer = currentPlayer;
        this.gameOver = false;
    }

    @Override
    public String getType() { return "ATTACK_RESULT"; }

    public String getResult() { return result; }

    public void setResult(String result) { this.result = result; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getDestroyedShip() { return destroyedShip; }
    public void setDestroyedShip(String destroyedShip) { this.destroyedShip = destroyedShip; }
}