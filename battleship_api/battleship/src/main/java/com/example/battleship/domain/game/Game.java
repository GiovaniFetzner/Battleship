package com.example.battleship.domain.game;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.exception.InvalidMoveException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Game {

    private final String id;
    private final Player player1;
    private Player player2;

    private final Set<String> readyPlayers = new HashSet<>();

    private Player currentPlayer;
    private GameState state;
    private Player winner;

    private Turn currentTurn;
    private int turnCounter;

    public Game(Player player1) {
        this.id = UUID.randomUUID().toString();
        this.player1 = player1;
        this.state = GameState.WAITING_FOR_PLAYERS;
    }

    /* =============================
       PLAYER MANAGEMENT
       ============================= */

    public void addPlayer2(Player player) {

        if (state != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidMoveException("Cannot add player at this stage");
        }

        if (this.player2 != null) {
            throw new InvalidMoveException("Game is already full");
        }

        if (player1.getName().equals(player.getName())) {
            throw new InvalidMoveException("Player name already taken");
        }

        this.player2 = player;
        this.state = GameState.PLACING_SHIPS;
    }


    public Player findPlayer(String playerName) {

        if (player1.getName().equals(playerName)) {
            return player1;
        }

        if (player2 != null && player2.getName().equals(playerName)) {
            return player2;
        }

        throw new IllegalArgumentException("Player not found in this game");
    }


    public Player getOpponent(Player player) {

        if (player2 == null) {
            throw new InvalidMoveException("Game does not have two players yet");
        }

        if (player1.getName().equals(player.getName())) {
            return player2;
        }

        if (player2.getName().equals(player.getName())) {
            return player1;
        }

        throw new IllegalArgumentException("Player not in this game");
    }


    /* =============================
       SHIP PLACEMENT PHASE
       ============================= */

    public synchronized void placeShip(
            String playerName,
            Ship ship,
            Coordinate coordinate,
            Orientation orientation) {

        if (state != GameState.PLACING_SHIPS) {
            throw new InvalidMoveException("Not in ship placement phase");
        }

        Player player = findPlayer(playerName);

        player.placeShip(ship, coordinate, orientation);
    }

    public boolean canPlaceShips() {
        return state == GameState.PLACING_SHIPS;
    }

    public synchronized void confirmPlacement(String playerName) {

        Player player = findPlayer(playerName);

        player.confirmShipsPlacement();
        markPlayerReady(playerName);
    }


    public synchronized void markPlayerReady(String playerName) {

        if (state != GameState.PLACING_SHIPS) {
            throw new InvalidMoveException("Not in ship placement phase");
        }

        Player player = findPlayer(playerName);

        if (!player.hasPlacedShips()) {
            throw new InvalidMoveException("Player has not placed all ships");
        }

        readyPlayers.add(player.getName());

        if (areBothPlayersReady()) {
            startBattle();
        }
    }



    private boolean areBothPlayersReady() {
        return player2 != null && readyPlayers.size() == 2;
    }

    private void startBattle() {

        this.state = GameState.IN_PROGRESS;
        this.currentPlayer = player1; // pode trocar por sorteio depois
        this.turnCounter = 1;
        this.currentTurn = new Turn(currentPlayer, turnCounter);
    }

    /* =============================
       BATTLE PHASE
       ============================= */

    public AttackResult attack(String playerName, Coordinate coordinate) {

        if (state != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is not in progress");
        }

        Player attacker = findPlayer(playerName);

        if (!currentPlayer.getName().equals(attacker.getName())) {
            throw new InvalidMoveException("It's not your turn");
        }

        Player opponent = getOpponent(attacker);

        AttackResult result = opponent.getBoard().attack(coordinate);

        if (opponent.getBoard().allShipsDestroyed()) {
            finishGame(attacker);
            return result;
        }

        nextTurn();

        return result;
    }


    private void nextTurn() {

        if (state == GameState.FINISHED) {
            return;
        }

        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        turnCounter++;
        currentTurn = new Turn(currentPlayer, turnCounter);
    }

    private void finishGame(Player winner) {
        this.state = GameState.FINISHED;
        this.winner = winner;
    }

    /* =============================
       GETTERS
       ============================= */

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

    public boolean isGameOver() {
        return state == GameState.FINISHED;
    }

    public boolean canAttack() {
        return state == GameState.IN_PROGRESS;
    }

    public int getTurnCounter() {
        return currentTurn != null ? currentTurn.turnNumber() : 0;
    }

    public String getId() {
        return id;
    }
}
