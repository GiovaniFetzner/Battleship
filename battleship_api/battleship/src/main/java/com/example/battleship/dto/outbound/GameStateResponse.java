package com.example.battleship.dto.outbound;

import java.util.List;

public class GameStateResponse {
    private String gameId;
    private String state; // "WAITING", "IN_PROGRESS", "FINISHED"
    private String player1;
    private String player2;
    private String currentPlayer;
    private int turnNumber;
    private boolean gameOver;
    private String winner;
    private List<ShipDTO> myShips;
    private List<AttackDTO> myAttacks;
    private List<AttackDTO> opponentAttacks;

    public GameStateResponse() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<ShipDTO> getMyShips() {
        return myShips;
    }

    public void setMyShips(List<ShipDTO> myShips) {
        this.myShips = myShips;
    }

    public List<AttackDTO> getMyAttacks() {
        return myAttacks;
    }

    public void setMyAttacks(List<AttackDTO> myAttacks) {
        this.myAttacks = myAttacks;
    }

    public List<AttackDTO> getOpponentAttacks() {
        return opponentAttacks;
    }

    public void setOpponentAttacks(List<AttackDTO> opponentAttacks) {
        this.opponentAttacks = opponentAttacks;
    }
}
