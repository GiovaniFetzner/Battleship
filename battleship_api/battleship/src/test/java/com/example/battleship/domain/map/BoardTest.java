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

    @Test
    void shouldThrowExceptionWhenAttackingSameCellTwice() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(3);

        board.placeShip(ship, new Coordinate(5, 5));
        board.attack(new Coordinate(5, 5));

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.attack(new Coordinate(5, 5))
        );

        assertEquals("Cell already attacked!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAttackingEmptyCellTwice() {
        Board board = new Board(10, 10);

        board.attack(new Coordinate(3, 3));

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.attack(new Coordinate(3, 3))
        );

        assertEquals("Cell already attacked!", exception.getMessage());
    }

    @Test
    void shouldReturnFalseWhenAllShipsAreNotDestroyed() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship(2);
        Ship ship2 = new Ship(3);

        board.placeShip(ship1, new Coordinate(1, 1));
        board.placeShip(ship2, new Coordinate(5, 5));

        Assertions.assertFalse(board.allShipsDestroyed(), "Board should have ships");
    }

    @Test
    void shouldReturnTrueWhenAllShipsAreDestroyed() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship(1);
        Ship ship2 = new Ship(1);

        board.placeShip(ship1, new Coordinate(1, 1));
        board.placeShip(ship2, new Coordinate(5, 5));

        board.attack(new Coordinate(1, 1));
        board.attack(new Coordinate(5, 5));

        Assertions.assertTrue(board.allShipsDestroyed(), "All ships should be destroyed");
    }

    @Test
    void shouldReturnTrueForEmptyBoard() {
        Board board = new Board(10, 10);

        Assertions.assertTrue(board.allShipsDestroyed(), "Empty board should have no ships");
    }

    @Test
    void shouldDestroyShipAfterMultipleHits() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(3);

        board.placeShip(ship, new Coordinate(3, 3));

        AttackResult result1 = board.attack(new Coordinate(3, 3));
        AttackResult result2 = board.attack(new Coordinate(3, 4));
        AttackResult result3 = board.attack(new Coordinate(3, 5));

        assertEquals(AttackResult.HIT, result1, "First hit should return HIT");
        assertEquals(AttackResult.HIT, result2, "Second hit should return HIT");
        assertEquals(AttackResult.DESTROYED, result3, "Third hit should return DESTROYED");
    }

    @Test
    void shouldHandleMultipleShipsOnBoard() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Porta-Avioes", 5);
        Ship ship2 = new Ship("Bombardeiro", 4);
        Ship ship3 = new Ship("Submarino", 3);

        board.placeShip(ship1, new Coordinate(0, 0));
        board.placeShip(ship2, new Coordinate(5, 5));
        board.placeShip(ship3, new Coordinate(8, 8));

        AttackResult result1 = board.attack(new Coordinate(0, 0));
        assertEquals(AttackResult.HIT, result1);

        AttackResult result2 = board.attack(new Coordinate(5, 5));
        assertEquals(AttackResult.HIT, result2);

        AttackResult result3 = board.attack(new Coordinate(2, 2));
        assertEquals(AttackResult.MISS, result3);

        Assertions.assertFalse(board.allShipsDestroyed(), "Ships should still exist");
    }

    @Test
    void shouldNotAllowPlacingShipWithNegativeCoordinates() {
        Board board = new Board(10, 10);
        Ship ship = new Ship(2);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(-1, 5))
        );

        assertEquals("Ship cannot be placed outside the board!", exception.getMessage());
    }

    @Test
    void shouldNotAllowAttackWithNegativeCoordinates() {
        Board board = new Board(10, 10);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.attack(new Coordinate(5, -1))
        );

        assertEquals("Attack outside board, please review the coordinates!", exception.getMessage());
    }

}
