package com.example.battleship.domain.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoardTest {

    @Test
    void shouldReturnMIssWhenAttackingEmptyCell(){
        Board board = new Board(10, 10);

        AttackResult result = board.attack(new Coordinate(3, 4));

        Assertions.assertEquals(AttackResult.MISS, result);

    }
}
