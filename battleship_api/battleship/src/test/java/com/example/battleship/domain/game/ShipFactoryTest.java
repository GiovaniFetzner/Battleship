package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Ship;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipFactoryTest {

    @Test
    void shouldCreateDefaultShipsWithCorrectQuantity() {
        List<Ship> ships = ShipFactory.createDefaultShips();

        assertEquals(4, ships.size(), "Should create four ships");
    }

    @Test
    void shouldCreateShipsWithCorrectSizes() {
        List<Ship> ships = ShipFactory.createDefaultShips();

        assertEquals(5, ships.get(0).getSize(), "Porta-Aviões should have size of five");
        assertEquals(4, ships.get(1).getSize(), "Bombardeiro  should have size of four");
        assertEquals(3, ships.get(2).getSize(), "Submarino  should have size of three");
        assertEquals(2, ships.get(3).getSize(), "Lancha Militar  should have size of two");
    }

    @Test
    void shouldCreateShipsWithCorrectNames() {
        List<Ship> ships = ShipFactory.createDefaultShips();

        assertEquals("Porta-Aviões", ships.get(0).getName());
        assertEquals("Bombardeiro", ships.get(1).getName());
        assertEquals("Submarino", ships.get(2).getName());
        assertEquals("Lancha Militar", ships.get(3).getName());
    }

    @Test
    void shouldCreateShipsWithZeroHits() {
        List<Ship> ships = ShipFactory.createDefaultShips();

        for (Ship ship : ships) {
            assertEquals(0, ship.getHits(), ship.getName() + "Should begin healthy: 0 hits");
        }
    }

    @Test
    void shouldCreateNewInstancesEachTime() {
        List<Ship> ships1 = ShipFactory.createDefaultShips();
        List<Ship> ships2 = ShipFactory.createDefaultShips();

        assertNotSame(ships1, ships2, "Should create a new list of ships each time");
        assertNotSame(ships1.get(0), ships2.get(0), "Checking the new instances");
    }
}
