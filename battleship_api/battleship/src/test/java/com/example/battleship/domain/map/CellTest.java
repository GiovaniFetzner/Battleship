package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Test
    void shouldMarkCellAsHitWhenAttacked() {
        Cell cell = new Cell();

        AttackResult result = cell.attack();

        assertTrue(cell.isAttacked());
        assertEquals(AttackResult.MISS, result);
    }

    @Test
    void shouldNotAllowAttackingSameCellTwice() {
        Cell cell = new Cell();

        cell.attack();

        assertThrows(
                InvalidMoveException.class,
                cell::attack
        );
    }


    @Test
    void shouldReturnHitWhenAttackingCellWithShip() {
        Cell cell = new Cell();
        Ship ship = new Ship(1);

        cell.placeShip(ship);

        AttackResult result = cell.attack();

        assertEquals(AttackResult.DESTROYED, result);
    }

}

