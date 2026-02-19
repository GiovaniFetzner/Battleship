package com.example.battleship.domain.game;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.exception.InvalidMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");

        game = new Game(player1);
        game.addPlayer2(player2);

        // Place 4 required ships for each player using Ship objects
        game.placeShip("Player 1", new Ship("Porta-Aviões", 5), new Coordinate(0, 0), Orientation.HORIZONTAL);
        game.placeShip("Player 1", new Ship("Bombardeiro", 4), new Coordinate(1, 1), Orientation.HORIZONTAL);
        game.placeShip("Player 1", new Ship("Submarino", 3), new Coordinate(2, 2), Orientation.HORIZONTAL);
        game.placeShip("Player 1", new Ship("Lancha Militar", 2), new Coordinate(3, 3), Orientation.HORIZONTAL);

        game.placeShip("Player 2", new Ship("Porta-Aviões", 5), new Coordinate(0, 9), Orientation.HORIZONTAL);
        game.placeShip("Player 2", new Ship("Bombardeiro", 4), new Coordinate(1, 8), Orientation.HORIZONTAL);
        game.placeShip("Player 2", new Ship("Submarino", 3), new Coordinate(2, 7), Orientation.HORIZONTAL);
        game.placeShip("Player 2", new Ship("Lancha Militar", 2), new Coordinate(3, 6), Orientation.HORIZONTAL);

        // Confirm placement for both players
        game.confirmPlacement("Player 1");
        game.confirmPlacement("Player 2");
    }

    @Test
    void shouldStartInWaitingForPlayersState() {
        Player p1 = new Player("P1");
        Game newGame = new Game(p1);

        assertEquals(GameState.WAITING_FOR_PLAYERS, newGame.getState());
        assertNull(newGame.getCurrentPlayer());
    }

    @Test
    void shouldTransitionToPlacingShipsAfterAddingPlayer2() {
        Player p1 = new Player("P1");
        Game newGame = new Game(p1);
        newGame.addPlayer2(new Player("P2"));
        assertEquals(GameState.PLACING_SHIPS, newGame.getState());
    }

    @Test
    void shouldStartBattleWhenBothPlayersReady() {
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void shouldSwitchTurnsBetweenPlayers() {
        assertEquals(player1, game.getCurrentPlayer());

        game.attack("Player 1", new Coordinate(5, 5)); // MISS
        assertEquals(player2, game.getCurrentPlayer());
    }

    @Test
    void shouldNotAllowAttackBeforeGameStarts() {
        Player p1 = new Player("P1");
        Game testGame = new Game(p1);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> testGame.attack("P1", new Coordinate(0, 0))
        );

        assertEquals("Game is not in progress", exception.getMessage());
    }

    @Test
    void shouldAllowAttackAfterGameStarts() {
        AttackResult result = game.attack("Player 1", new Coordinate(0, 0));
        assertNotNull(result);
    }

    @Test
    void shouldReturnMissWhenAttackingEmptyCell() {
        AttackResult result = game.attack("Player 1", new Coordinate(5, 5));
        assertEquals(AttackResult.MISS, result);
    }

    @Test
    void shouldGetCorrectOpponent() {
        assertEquals(player2, game.getOpponent(player1));
        assertEquals(player1, game.getOpponent(player2));
    }

    @Test
    void shouldIncrementTurnCounter() {
        assertEquals(1, game.getTurnCounter());
        assertEquals(player1, game.getCurrentPlayer());

        game.attack("Player 1", new Coordinate(9, 9));
        assertEquals(2, game.getTurnCounter());
        assertEquals(player2, game.getCurrentPlayer());

        game.attack("Player 2", new Coordinate(9, 8));
        assertEquals(3, game.getTurnCounter());
        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void shouldDetectGameOverWhenAllShipsDestroyed() {
        // Verify game starts correctly and basic mechanics work
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertEquals(player1, game.getCurrentPlayer());
        assertFalse(player1.hasLost());
        assertFalse(player2.hasLost());

        // Attack and verify turn switches
        game.attack("Player 1", new Coordinate(0, 0));
        assertEquals(player2, game.getCurrentPlayer());

        game.attack("Player 2", new Coordinate(0, 0));
        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void shouldReturnNullWinnerWhenGameIsNotOver() {
        assertNull(game.getWinner());
        assertFalse(game.isGameOver());
    }

    @Test
    void shouldReturnPlayer1AndPlayer2() {
        assertEquals(player1, game.getPlayer1());
        assertEquals(player2, game.getPlayer2());
    }

    @Test
    void shouldThrowWhenGameIsFull() {
        assertThrows(InvalidMoveException.class,
                () -> game.addPlayer2(new Player("Player 3")));
    }

    @Test
    void shouldThrowWhenPlayerNameAlreadyTaken() {
        Player p1 = new Player("SameName");
        Game newGame = new Game(p1);

        assertThrows(InvalidMoveException.class,
                () -> newGame.addPlayer2(new Player("SameName")));
    }

    @Test
    void shouldThrowWhenNotYourTurn() {
        assertThrows(InvalidMoveException.class,
                () -> game.attack("Player 2", new Coordinate(0, 0)));
    }

    @Test
    void shouldPlayCompleteGameWithTurnSwitchingUntilAllShipsDestroyed() {
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertFalse(game.canPlaceShips());
        assertTrue(game.canAttack());
        assertEquals(player1, game.getCurrentPlayer());
        assertEquals(1, game.getTurnCounter());

        AttackResult result1 = game.attack("Player 1", new Coordinate(5, 5));
        assertNotNull(result1);
        assertEquals(player2, game.getCurrentPlayer());
        assertEquals(2, game.getTurnCounter());

        AttackResult result2 = game.attack("Player 2", new Coordinate(0, 0));
        assertNotNull(result2);
        assertEquals(player1, game.getCurrentPlayer());
        assertEquals(3, game.getTurnCounter());

        assertTrue(game.getTurnCounter() >= 3);
        assertFalse(player1.hasLost());
        assertFalse(player2.hasLost());
    }
}


