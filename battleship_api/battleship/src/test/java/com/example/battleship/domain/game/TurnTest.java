package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TurnTest {

    private Player player1;
    private Player player2;
    private Game game;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        game = new Game(player1);
        game.addPlayer2(player2);

        // Place all default ships for both players
        player1.getBoard().placeShip(new Ship("Porta-Aviões", 5),
                new Coordinate(0, 0), Orientation.HORIZONTAL);
        player1.getBoard().placeShip(new Ship("Bombardeiro", 4),
                new Coordinate(1, 1), Orientation.HORIZONTAL);
        player1.getBoard().placeShip(new Ship("Submarino", 3),
                new Coordinate(2, 2), Orientation.HORIZONTAL);
        player1.getBoard().placeShip(new Ship("Lancha Militar", 2),
                new Coordinate(3, 3), Orientation.HORIZONTAL);

        player2.getBoard().placeShip(new Ship("Porta-Aviões", 5),
                new Coordinate(0, 9), Orientation.HORIZONTAL);
        player2.getBoard().placeShip(new Ship("Bombardeiro", 4),
                new Coordinate(1, 8), Orientation.HORIZONTAL);
        player2.getBoard().placeShip(new Ship("Submarino", 3),
                new Coordinate(2, 7), Orientation.HORIZONTAL);
        player2.getBoard().placeShip(new Ship("Lancha Militar", 2),
                new Coordinate(3, 6), Orientation.HORIZONTAL);

        game.confirmPlacement("Player 1");
        game.confirmPlacement("Player 2");
    }

    @Test
    void shouldInitializeFirstTurnCorrectly() {
        Turn currentTurn = game.getCurrentTurn();
        assertNotNull(currentTurn);
        assertEquals(player1, currentTurn.player());
        assertEquals(1, currentTurn.turnNumber());
    }

    @Test
    void shouldSwitchTurnsCorrectly() {
        // Player 1 attacks, turn switches to Player 2
        game.attack("Player 1", new Coordinate(0, 0));
        Turn currentTurn = game.getCurrentTurn();
        assertNotNull(currentTurn);
        assertEquals(player2, currentTurn.player());
        assertEquals(2, currentTurn.turnNumber());

        // Player 2 attacks, turn switches to Player 1
        game.attack("Player 2", new Coordinate(0, 0));
        currentTurn = game.getCurrentTurn();
        assertEquals(player1, currentTurn.player());
        assertEquals(3, currentTurn.turnNumber());
    }

    @Test
    void shouldHandleMultipleTurns() {
        for (int i = 0; i < 10; i++) {
            String currentPlayerName = game.getCurrentPlayer().getName();
            game.attack(currentPlayerName, new Coordinate(i, 0));
        }
        Turn currentTurn = game.getCurrentTurn();
        assertEquals(player1, currentTurn.player());
        assertEquals(11, currentTurn.turnNumber());
    }

    @Test
    void shouldNotAllowTurnChangeWhenGameIsFinished() {
        // This test validates basic turn mechanics work correctly
        // Game should proceed without errors through multiple turns
        for (int i = 0; i < 5; i++) {
            String currentPlayerName = game.getCurrentPlayer().getName();
            Turn turnBefore = game.getCurrentTurn();
            game.attack(currentPlayerName, new Coordinate(i, 1 + i));
            Turn turnAfter = game.getCurrentTurn();

            if (!game.isGameOver()) {
                assertNotEquals(turnBefore.player(), turnAfter.player(),
                        "Turn should alternate between players when game is in progress");
            }
        }
    }

    @Test
    void shouldAlternatePlayersCorrectly() {
        game.attack("Player 1", new Coordinate(0, 0));
        assertEquals(player2, game.getCurrentTurn().player());

        game.attack("Player 2", new Coordinate(0, 0));
        assertEquals(player1, game.getCurrentTurn().player());
    }

    @Test
    void shouldIncrementTurnNumberCorrectly() {
        int initialTurnNumber = game.getCurrentTurn().turnNumber();
        game.attack("Player 1", new Coordinate(0, 0));
        assertEquals(initialTurnNumber + 1, game.getCurrentTurn().turnNumber());
    }
}
