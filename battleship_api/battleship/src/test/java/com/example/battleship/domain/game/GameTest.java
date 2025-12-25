package com.example.battleship.domain.game;

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
}
