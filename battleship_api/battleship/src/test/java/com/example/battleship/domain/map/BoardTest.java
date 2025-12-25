package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BoardTest {

    @Test
    void shouldReturnMIssWhenAttackingEmptyCell(){
        Board board = new Board(10, 10);

        AttackResult result = board.attack(new Coordinate(3, 4));

        assertEquals(AttackResult.MISS, result);

    }

    @Test
    void shouldThrowExceptionWhenAttackingOutsideBoard(){
        Board board = new Board(10,10);

        Assertions.assertThrows(
                InvalidMoveException.class,
                () -> board.attack(new Coordinate(20, 5))
        );
    }

    @Test
    void shouldPlaceShipOnBoard() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(1);

        board.placeShip(ship, new Coordinate(2, 3));

        AttackResult result = board.attack(new Coordinate(2, 3));

        assertEquals(AttackResult.DESTROYED, result);
    }

    @Test
    void shouldNotPlaceShipOverlappingAnotherShip() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship(3);
        Ship ship2 = new Ship(2);

        board.placeShip(ship1, new Coordinate(2, 2));

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship2, new Coordinate(2, 2))
        );

        assertEquals("Cannot place a ship on top of another ship!", exception.getMessage());
    }

    @Test
    void shouldHandleAttackOnShipPart() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(3);

        board.placeShip(ship, new Coordinate(4, 4));

        AttackResult result = board.attack(new Coordinate(4, 4));

        assertEquals(AttackResult.HIT, result);
    }

    @Test
    void shouldNotPlaceShipOutsideBoard() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(3);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(12, 12))
        );

        assertEquals("Ship cannot be placed outside the board!", exception.getMessage());
    }

}
