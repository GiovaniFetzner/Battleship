package com.example.battleship.dto.rest.outbound;

public class GameStateResponse {

    private String gameId;
    private String gameStatus;

    private String player1Name;
    private String player2Name;

    private String currentPlayer;
    private boolean myTurn;

    private String winner;

    private int turnNumber;

    private int myShipsRemaining;
    private int opponentShipsRemaining;

    public GameStateResponse() {}

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }

    public String getPlayer1Name() { return player1Name; }
    public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }

    public String getPlayer2Name() { return player2Name; }
    public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isMyTurn() { return myTurn; }
    public void setMyTurn(boolean myTurn) { this.myTurn = myTurn; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public int getMyShipsRemaining() { return myShipsRemaining; }
    public void setMyShipsRemaining(int myShipsRemaining) { this.myShipsRemaining = myShipsRemaining; }

    public int getOpponentShipsRemaining() { return opponentShipsRemaining; }
    public void setOpponentShipsRemaining(int opponentShipsRemaining) { this.opponentShipsRemaining = opponentShipsRemaining; }
}
