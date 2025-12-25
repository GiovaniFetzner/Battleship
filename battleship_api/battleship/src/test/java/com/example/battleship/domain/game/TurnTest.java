package com.example.battleship.domain.game;

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
        game = new Game(player1, player2);
        game.start();
    }

    @Test
    void shouldInitializeFirstTurnCorrectly() {
        Turn currentTurn = game.getCurrentTurn();
        assertNotNull(currentTurn);
        assertEquals(player1, currentTurn.getPlayer());
        assertEquals(1, currentTurn.getTurnNumber());
    }

    @Test
    void shouldSwitchTurnsCorrectly() {
        game.nextTurn();
        Turn currentTurn = game.getCurrentTurn();
        assertNotNull(currentTurn);
        assertEquals(player2, currentTurn.getPlayer());
        assertEquals(2, currentTurn.getTurnNumber());

        game.nextTurn();
        currentTurn = game.getCurrentTurn();
        assertEquals(player1, currentTurn.getPlayer());
        assertEquals(3, currentTurn.getTurnNumber());
    }

    @Test
    void shouldHandleMultipleTurns() {
        for (int i = 0; i < 10; i++) {
            game.nextTurn();
        }
        Turn currentTurn = game.getCurrentTurn();
        assertEquals(player1, currentTurn.getPlayer());
        assertEquals(11, currentTurn.getTurnNumber());
    }
}
