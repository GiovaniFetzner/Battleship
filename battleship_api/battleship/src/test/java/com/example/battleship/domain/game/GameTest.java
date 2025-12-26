package com.example.battleship.domain.game;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
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

        player1.setShips(ShipFactory.createDefaultShips());
        player2.setShips(ShipFactory.createDefaultShips());

        game = new Game(player1, player2);
    }

    @Test
    void shouldStartGameWithTwoPlayers() {
        assertEquals(GameState.WAITING, game.getState());
        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void shouldSwitchTurnsBetweenPlayers() {
        game.start();

        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertEquals(player1, game.getCurrentPlayer());

        game.nextTurn();
        assertEquals(player2, game.getCurrentPlayer());
    }

    @Test
    void shouldEndGameWhenOnePlayerLosesAllShips() {
        player2.loseAllShips();

        Player winner = game.checkWinner();

        assertEquals(player1, winner, "The winner should be Player 1 when Player 2 loses all ships.");
    }

    @Test
    void shouldNotAllowAttackBeforeGameStarts() {
        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> game.attack(new Coordinate(0, 0))
        );

        assertEquals("Game is not in progress!", exception.getMessage());
    }

    @Test
    void shouldAllowAttackAfterGameStarts() {
        game.start();

        player2.getBoard().placeShip(ShipFactory.createDefaultShips().get(0),
                                     new Coordinate(0, 0), Orientation.HORIZONTAL);

        AttackResult result = game.attack(new Coordinate(0, 0));

        assertEquals(AttackResult.HIT, result);
    }

    @Test
    void shouldReturnMissWhenAttackingEmptyCell() {
        game.start();

        AttackResult result = game.attack(new Coordinate(5, 5));

        assertEquals(AttackResult.MISS, result);
    }

    @Test
    void shouldGetCorrectOpponent() {
        assertEquals(player2, game.getOpponent(player1));
        assertEquals(player1, game.getOpponent(player2));
    }

    @Test
    void shouldIncrementTurnCounter() {
        game.start();

        assertEquals(1, game.getTurnCounter());

        game.nextTurn();
        assertEquals(2, game.getTurnCounter());

        game.nextTurn();
        assertEquals(3, game.getTurnCounter());
    }

    @Test
    void shouldNotSwitchTurnWhenGameIsFinished() {
        game.start();
        player2.loseAllShips();
        game.checkGameOver();

        assertEquals(player1, game.getCurrentPlayer());

        game.nextTurn();

        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void shouldSetWinnerWhenGameEnds() {
        game.start();
        player2.loseAllShips();
        game.checkGameOver();

        assertEquals(GameState.FINISHED, game.getState());
        assertEquals(player1, game.getWinner());
    }

    @Test
    void shouldDetectGameOverWhenAllShipsDestroyed() {
        game.start();

        player2.getBoard().placeShip(ShipFactory.createDefaultShips().get(3),
                                     new Coordinate(0, 0), Orientation.HORIZONTAL);

        game.attack(new Coordinate(0, 0));
        game.attack(new Coordinate(1, 0));

        assertTrue(game.isGameOver());
        assertEquals(GameState.FINISHED, game.getState());
        assertEquals(player1, game.getWinner());
    }

    @Test
    void shouldReturnNullWinnerWhenGameIsNotOver() {
        game.start();

        assertNull(game.getWinner());
        assertFalse(game.isGameOver());
    }

    @Test
    void shouldAllowPlaceShipsBeforeGameStarts() {
        assertTrue(game.canPlaceShips());

        game.start();

        assertFalse(game.canPlaceShips());
    }

    @Test
    void shouldAllowAttackOnlyDuringGame() {
        assertFalse(game.canAttack());

        game.start();

        assertTrue(game.canAttack());

        player2.loseAllShips();
        game.checkGameOver();

        assertFalse(game.canAttack());
    }

    @Test
    void shouldReturnPlayer1AndPlayer2() {
        assertEquals(player1, game.getPlayer1());
        assertEquals(player2, game.getPlayer2());
    }

    @Test
    void shouldCreateCurrentTurnWhenGameStarts() {
        assertNull(game.getCurrentTurn());

        game.start();

        assertNotNull(game.getCurrentTurn());
        assertEquals(player1, game.getCurrentTurn().getPlayer());
        assertEquals(1, game.getCurrentTurn().getTurnNumber());
    }

    @Test
    void shouldPlayCompleteGameWithTurnSwitchingUntilAllShipsDestroyed() {
        // === Round 1: Ship placement ===

        // Player 1
        player1.getBoard().placeShip(new com.example.battleship.domain.map.Ship("Destroyer", 2),
                                     new Coordinate(0, 0), Orientation.HORIZONTAL); // (0,0), (1,0)
        player1.getBoard().placeShip(new com.example.battleship.domain.map.Ship("Cruiser", 3),
                                     new Coordinate(0, 2), Orientation.VERTICAL); // (0,2), (0,3), (0,4)

        // Player 2
        player2.getBoard().placeShip(new com.example.battleship.domain.map.Ship("Destroyer", 2),
                                     new Coordinate(5, 5), Orientation.HORIZONTAL); // (5,5), (6,5)
        player2.getBoard().placeShip(new com.example.battleship.domain.map.Ship("Cruiser", 3),
                                     new Coordinate(8, 7), Orientation.VERTICAL); // (8,7), (8,8), (8,9)

        // Initial game status
        assertTrue(game.canPlaceShips(), "Should allow placing ships before game starts");
        assertEquals(GameState.WAITING, game.getState());

        // === Round 2: Game starts ===
        game.start();

        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertFalse(game.canPlaceShips(), "Should not allow placing ships after game starts");
        assertTrue(game.canAttack(), "Should allow attacks during game");
        assertEquals(player1, game.getCurrentPlayer());
        assertEquals(1, game.getTurnCounter());

        // === Round 3 switching attacks  ===

        // Turn 1 - Player 1
        AttackResult result1 = game.attack(new Coordinate(5, 5));
        assertEquals(AttackResult.HIT, result1);
        assertEquals(player1, game.getCurrentPlayer());

        game.nextTurn();
        assertEquals(player2, game.getCurrentPlayer());
        assertEquals(2, game.getTurnCounter());

        // Turn 2 - Player 2
        AttackResult result2 = game.attack(new Coordinate(0, 0));
        assertEquals(AttackResult.HIT, result2);
        assertFalse(game.isGameOver(), "Game should not be over yet");

        game.nextTurn();
        assertEquals(player1, game.getCurrentPlayer());
        assertEquals(3, game.getTurnCounter());

        // Turn 3 - Player 1 attack and destroy destroyer from player2
        AttackResult result3 = game.attack(new Coordinate(6, 5)); // DESTROYED
        assertEquals(AttackResult.DESTROYED, result3);
        assertFalse(game.isGameOver(), "Game should not be over - player2 still has cruiser");

        game.nextTurn();
        assertEquals(player2, game.getCurrentPlayer());

        // Turn 4 - Player 2 miss the shot
        AttackResult result4 = game.attack(new Coordinate(5, 0)); // MISS
        assertEquals(AttackResult.MISS, result4);

        game.nextTurn();
        assertEquals(player1, game.getCurrentPlayer());

        // Turn 5 - Player 1 attack cruiser from player2
        AttackResult result5 = game.attack(new Coordinate(8, 7)); // HIT
        assertEquals(AttackResult.HIT, result5);

        game.nextTurn();
        assertEquals(player2, game.getCurrentPlayer());

        // Turn 6 - Player 2 attack
        AttackResult result6 = game.attack(new Coordinate(1, 0)); // DESTROYED destroyer from player1
        assertEquals(AttackResult.DESTROYED, result6);
        assertFalse(game.isGameOver(), "Game should not be over - player1 still has cruiser");

        game.nextTurn();
        assertEquals(player1, game.getCurrentPlayer());

        // Turn 7 - Player 1 attack cruiser from player2
        AttackResult result7 = game.attack(new Coordinate(8, 8)); // HIT
        assertEquals(AttackResult.HIT, result7);

        game.nextTurn();
        assertEquals(player2, game.getCurrentPlayer());

        // Turn 8 - Player 2 miss the shot
        AttackResult result8 = game.attack(new Coordinate(9, 9)); // MISS
        assertEquals(AttackResult.MISS, result8);

        game.nextTurn();
        assertEquals(player1, game.getCurrentPlayer());

        // Turn 9 - Player 1 destroy cruiser from player2 and wins
        AttackResult result9 = game.attack(new Coordinate(8, 9)); // DESTROYED
        assertEquals(AttackResult.DESTROYED, result9);

        // === Round 4: Check game status ===
        // Player 2 lost all the ships!
        assertTrue(game.isGameOver(), "Game should be over - all player2 ships destroyed");
        assertEquals(GameState.FINISHED, game.getState());
        assertEquals(player1, game.getWinner());
        assertTrue(player2.hasLost(), "Player 2 should have lost");
        assertFalse(player1.hasLost(), "Player 1 should not have lost");

        // Isn't possible to attack anymore
        assertFalse(game.canAttack(), "Should not allow attacks after game ends");

        // Turn should not change after game ends
        Player currentBeforeSwitch = game.getCurrentPlayer();
        game.nextTurn();
        assertEquals(currentBeforeSwitch, game.getCurrentPlayer(),
                     "Current player should not change after game ends");

        // Turn counter should reflect all turns played
        assertTrue(game.getTurnCounter() >= 9, "Should have at least 9 turns");

        // Boards status
        assertTrue(player2.getBoard().allShipsDestroyed(),
                   "All player2 ships should be destroyed");
        assertFalse(player1.getBoard().allShipsDestroyed(),
                    "Player1 should still have ships remaining");
    }
}
