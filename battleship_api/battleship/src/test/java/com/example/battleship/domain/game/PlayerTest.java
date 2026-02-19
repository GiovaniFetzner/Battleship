package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
    }

    @Test
    void shouldCreatePlayerWithValidName() {
        assertEquals("Test Player", player.getName());
        assertNotNull(player.getBoard());
    }

    @Test
    void shouldThrowExceptionForNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Player(null));
    }

    @Test
    void shouldThrowExceptionForBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Player("   "));
    }

    @Test
    void shouldNotHaveLostWhenShipsExist() {
        player.getBoard().placeShip(new Ship("Destroyer", 2),
                new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertFalse(player.hasLost(), "Player should not have lost when ships are still alive");
    }

    @Test
    void shouldHaveLostWhenAllShipsDestroyed() {
        player.getBoard().placeShip(new Ship("Destroyer", 2),
                new Coordinate(0, 0), Orientation.HORIZONTAL);

        // Attack all ship cells
        player.getBoard().attack(new Coordinate(0, 0));
        player.getBoard().attack(new Coordinate(1, 0));

        assertTrue(player.hasLost(), "Player should have lost when all ships are destroyed");
    }

    @Test
    void shouldNotHaveLostWhenOnlyPartiallyDamaged() {
        player.getBoard().placeShip(new Ship("Destroyer", 2),
                new Coordinate(0, 0), Orientation.HORIZONTAL);

        // Attack only one cell
        player.getBoard().attack(new Coordinate(0, 0));

        assertFalse(player.hasLost(), "Player should not have lost with partially damaged ship");
    }

    @Test
    void shouldHaveLostWhenMultipleShipsAllDestroyed() {
        player.getBoard().placeShip(new Ship("Destroyer", 2),
                new Coordinate(0, 0), Orientation.HORIZONTAL);
        player.getBoard().placeShip(new Ship("Cruiser", 3),
                new Coordinate(5, 5), Orientation.VERTICAL);

        // Destroy destroyer
        player.getBoard().attack(new Coordinate(0, 0));
        player.getBoard().attack(new Coordinate(1, 0));

        assertFalse(player.hasLost(), "Player should not have lost - cruiser still alive");

        // Destroy cruiser
        player.getBoard().attack(new Coordinate(5, 5));
        player.getBoard().attack(new Coordinate(5, 6));
        player.getBoard().attack(new Coordinate(5, 7));

        assertTrue(player.hasLost(), "Player should have lost when all ships are destroyed");
    }
}