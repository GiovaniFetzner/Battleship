package com.example.battleship.dto.outbound;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@JsonTypeName("GAME_STATE")
public class GameStateResponse extends GameBaseMessageResponse {

    private String gameId;
    private String gameStatus; // "WAITING", "IN_PROGRESS", "FINISHED"
    private String player1Id;
    private String player1Name;
    private String player2Id;
    private String player2Name;
    private String currentPlayer;
    private boolean isMyTurn;
    private String winner;
    private int turnNumber;
    private int myShipsRemaining;
    private int opponentShipsRemaining;
    private List<ShipDTO> myShips;
    private List<AttackDTO> myAttacks;
    private List<AttackDTO> opponentAttacks;

    public GameStateResponse() {}

    // Getters e Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }

    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }

    public String getPlayer1Name() { return player1Name; }
    public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }

    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }

    public String getPlayer2Name() { return player2Name; }
    public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isMyTurn() { return isMyTurn; }
    public void setMyTurn(boolean myTurn) { isMyTurn = myTurn; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public int getMyShipsRemaining() { return myShipsRemaining; }
    public void setMyShipsRemaining(int myShipsRemaining) { this.myShipsRemaining = myShipsRemaining; }

    public int getOpponentShipsRemaining() { return opponentShipsRemaining; }
    public void setOpponentShipsRemaining(int opponentShipsRemaining) { this.opponentShipsRemaining = opponentShipsRemaining; }

    public List<ShipDTO> getMyShips() { return myShips; }
    public void setMyShips(List<ShipDTO> myShips) { this.myShips = myShips; }

    public List<AttackDTO> getMyAttacks() { return myAttacks; }
    public void setMyAttacks(List<AttackDTO> myAttacks) { this.myAttacks = myAttacks; }

    public List<AttackDTO> getOpponentAttacks() { return opponentAttacks; }
    public void setOpponentAttacks(List<AttackDTO> opponentAttacks) { this.opponentAttacks = opponentAttacks; }

    @Override
    public String getType() {
        return "GAME_STATE";
    }
}