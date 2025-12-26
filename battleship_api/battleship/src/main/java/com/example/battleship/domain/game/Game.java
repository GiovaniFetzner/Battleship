package com.example.battleship.domain.game;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.exception.InvalidMoveException;

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

    public AttackResult attack(Coordinate coordinate) {
        if (state != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is not in progress!");
        }

        if (state == GameState.FINISHED) {
            throw new InvalidMoveException("Game is already finished!");
        }

        Player opponent = getOpponent(currentPlayer);
        AttackResult result = opponent.getBoard().attack(coordinate);

        if (opponent.getBoard().allShipsDestroyed()) {
            opponent.loseAllShips();
            checkGameOver();
        }

        return result;
    }

    public Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

    public void nextTurn() {
        if (state == GameState.FINISHED) {
            return;
        }
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
            winner = player2;
            return player2;
        }

        if (player2.hasLost()) {
            state = GameState.FINISHED;
            winner = player1;
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

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public boolean isGameOver() {
        return state == GameState.FINISHED;
    }

    public boolean canPlaceShips() {
        return state == GameState.WAITING;
    }

    public boolean canAttack() {
        return state == GameState.IN_PROGRESS && !isGameOver();
    }
}
