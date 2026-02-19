package com.example.battleship.domain.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipTest {

    @Test
    void shouldBeDestroyedAfterAllHits() {
        Ship ship = new Ship("Battleship", 2);

        ship.hit();
        assertFalse(ship.isDestroyed());

        ship.hit();
        assertTrue(ship.isDestroyed());
    }
}
