package com.example.battleship.domain.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellTest {

    @Test
    void shouldMarkCellAsHitWhenAttacked() {
        Cell cell = new Cell();

        AttackResult result = cell.attack();

        assertTrue(cell.isAttacked());
        assertEquals(AttackResult.MISS, result);
    }
}

