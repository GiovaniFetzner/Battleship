package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Ship;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private Player player;
    private List<Ship> ships;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
        ships = ShipFactory.createDefaultShips();
        player.setShips(ships);
    }

    @Test
    void shouldMarkPlayerAsLostWhenLoseAllShipsCalled() {
        assertFalse(player.hasLost(), "Player should not have lost initially");

        assertEquals(0, player.getShips().get(0).getHits());
        player.loseAllShips();
        assertEquals(5, player.getShips().get(0).getHits());

        assertTrue(player.hasLost(), "Player should have lost after loseAllShips is called");
    }

    @Test
    void shouldSetAllShipsToFullyDestroyedWhenLoseAllShipsCalled() {
        Ship portaAvies = ships.get(0);
        Ship bombardeiro = ships.get(1);
        Ship submarino = ships.get(2);

        assertEquals(0, portaAvies.getHits());
        assertEquals(0, bombardeiro.getHits());
        assertEquals(0, submarino.getHits());

        player.loseAllShips();

        assertEquals(portaAvies.getSize(), portaAvies.getHits(),
                "Porta-Avioes should have hits equal to its size");
        assertEquals(bombardeiro.getSize(), bombardeiro.getHits(),
                "Bombardeiro should have hits equal to its size");
        assertEquals(submarino.getSize(), submarino.getHits(),
                "Submarino should have hits equal to its size");
    }

    @Test
    void shouldHandleLoseAllShipsWithPartiallyDamagedShips() {
        Ship portaAvioes = ships.get(0);
        Ship bombardeiro = ships.get(1);

        portaAvioes.hit();
        portaAvioes.hit();
        bombardeiro.hit();

        assertEquals(2, portaAvioes.getHits());
        assertEquals(1, bombardeiro.getHits());

        player.loseAllShips();

        assertEquals(5, portaAvioes.getHits());
        assertEquals(4, bombardeiro.getHits());

        assertTrue(player.hasLost());
        assertEquals(portaAvioes.getSize(), portaAvioes.getHits(),
                "Battleship should be fully destroyed");
        assertEquals(bombardeiro.getSize(), bombardeiro.getHits(),
                "Cruiser should be fully destroyed");
        assertEquals(ships.get(2).getSize(), ships.get(2).getHits(),
                "Destroyer should be fully destroyed");
    }

    @Test
    void shouldNotHaveLostInitially() {
        assertFalse(player.hasLost(), "New player should not have lost");
    }
}

