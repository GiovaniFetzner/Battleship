package com.example.battleship.domain.game;

import java.util.Arrays;
import java.util.List;

public class Game {

    private final Player player1;
    private final Player player2;
    private Player currentPlayer;
    private GameState state;
    private Player winner;
    private final List<Player> players;
    private Turn currentTurn;
    private int turnCounter;

    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.players = Arrays.asList(player1, player2);
        this.state = GameState.WAITING;
        this.currentPlayer = player1;
    }

    public void start() {
        this.state = GameState.IN_PROGRESS;
        this.turnCounter = 1;
        this.currentTurn = new Turn(currentPlayer, turnCounter);
    }

    public void nextTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        turnCounter++;
        currentTurn = new Turn(currentPlayer, turnCounter);
    }

    public void checkGameOver() {
        if (player1.hasLost()) {
            state = GameState.FINISHED;
            winner = player2;
        } else if (player2.hasLost()) {
            state = GameState.FINISHED;
            winner = player1;
        } else {
            winner = null;
        }
    }

    public Player checkWinner() {
        if (player1.hasLost()) {
            state = GameState.FINISHED;
            return player2;
        }

        if (player2.hasLost()) {
            state = GameState.FINISHED;
            return player1;
        }

        return null;
    }

    public GameState getState() {
        return state;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getWinner() {
        return winner;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }
}
