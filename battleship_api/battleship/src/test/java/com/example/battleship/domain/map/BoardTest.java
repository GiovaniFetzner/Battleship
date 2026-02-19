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
        Ship ship = new Ship("Destroyer", 1);

        board.placeShip(ship, new Coordinate(2, 3), Orientation.HORIZONTAL);

        AttackResult result = board.attack(new Coordinate(2, 3));

        assertEquals(AttackResult.DESTROYED, result);
    }

    @Test
    void shouldNotPlaceShipOverlappingAnotherShip() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Cruiser", 3);
        Ship ship2 = new Ship("Destroyer", 2);

        board.placeShip(ship1, new Coordinate(2, 2), Orientation.HORIZONTAL);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship2, new Coordinate(2, 2), Orientation.HORIZONTAL)
        );

        assertEquals("Cannot place a ship on top of another ship!", exception.getMessage());
    }

    @Test
    void shouldHandleAttackOnShipPart() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        board.placeShip(ship, new Coordinate(4, 4), Orientation.HORIZONTAL);

        AttackResult result = board.attack(new Coordinate(4, 4));

        assertEquals(AttackResult.HIT, result);
    }

    @Test
    void shouldNotPlaceShipOutsideBoard() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(12, 12), Orientation.HORIZONTAL)
        );

        assertEquals("Position outside board!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAttackingSameCellTwice() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        board.placeShip(ship, new Coordinate(5, 5), Orientation.HORIZONTAL);
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
        Ship ship1 = new Ship("Destroyer", 2);
        Ship ship2 = new Ship("Cruiser", 3);

        board.placeShip(ship1, new Coordinate(1, 1), Orientation.HORIZONTAL);
        board.placeShip(ship2, new Coordinate(5, 5), Orientation.HORIZONTAL);

        Assertions.assertFalse(board.allShipsDestroyed(), "Board should have ships");
    }

    @Test
    void shouldReturnTrueWhenAllShipsAreDestroyed() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Ship1", 1);
        Ship ship2 = new Ship("Ship2", 1);

        board.placeShip(ship1, new Coordinate(1, 1), Orientation.HORIZONTAL);
        board.placeShip(ship2, new Coordinate(5, 5), Orientation.HORIZONTAL);

        board.attack(new Coordinate(1, 1));
        board.attack(new Coordinate(5, 5));

        Assertions.assertTrue(board.allShipsDestroyed(), "All ships should be destroyed");
    }

    @Test
    void shouldReturnFalseForEmptyBoard() {
        Board board = new Board(10, 10);

        Assertions.assertFalse(board.allShipsDestroyed(), "Empty board has no ships to be destroyed");
    }

    @Test
    void shouldDestroyShipAfterMultipleHits() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        board.placeShip(ship, new Coordinate(3, 3), Orientation.HORIZONTAL);

        assertEquals(0, ship.getHits(), "Ship should have 0 hits before attacking");

        AttackResult result1 = board.attack(new Coordinate(3, 3));
        AttackResult result2 = board.attack(new Coordinate(4, 3));
        AttackResult result3 = board.attack(new Coordinate(5, 3));

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

        board.placeShip(ship1, new Coordinate(0, 0), Orientation.HORIZONTAL); // Horizontal: (0,0) to (4,0)
        board.placeShip(ship2, new Coordinate(5, 5), Orientation.HORIZONTAL); // Horizontal: (5,5) to (8,5)
        board.placeShip(ship3, new Coordinate(7, 7), Orientation.HORIZONTAL); // Horizontal: (7,7) to (9,7)

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
        Ship ship = new Ship("Destroyer", 2);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(-1, 5), Orientation.HORIZONTAL)
        );

        assertEquals("Position outside board!", exception.getMessage());
    }

    @Test
    void shouldNotAllowAttackWithNegativeCoordinates() {
        Board board = new Board(10, 10);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.attack(new Coordinate(5, -1))
        );

        assertEquals("Position outside board!", exception.getMessage());
    }

    @Test
    void shouldPlaceShipHorizontally() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        board.placeShip(ship, new Coordinate(2, 2), Orientation.HORIZONTAL);

        AttackResult result1 = board.attack(new Coordinate(2, 2));
        AttackResult result2 = board.attack(new Coordinate(3, 2));
        AttackResult result3 = board.attack(new Coordinate(4, 2));

        assertEquals(AttackResult.HIT, result1);
        assertEquals(AttackResult.HIT, result2);
        assertEquals(AttackResult.DESTROYED, result3);
    }

    @Test
    void shouldPlaceShipVertically() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Battleship", 4);

        board.placeShip(ship, new Coordinate(5, 5), Orientation.VERTICAL);

        AttackResult result1 = board.attack(new Coordinate(5, 5));
        AttackResult result2 = board.attack(new Coordinate(5, 6));
        AttackResult result3 = board.attack(new Coordinate(5, 7));
        AttackResult result4 = board.attack(new Coordinate(5, 8));

        assertEquals(AttackResult.HIT, result1);
        assertEquals(AttackResult.HIT, result2);
        assertEquals(AttackResult.HIT, result3);
        assertEquals(AttackResult.DESTROYED, result4);
    }

    @Test
    void shouldNotPlaceHorizontalShipOutsideBoardBoundary() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Cruiser", 3);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(8, 5), Orientation.HORIZONTAL)
        );

        assertEquals("Ship cannot be placed outside the board!", exception.getMessage());
    }

    @Test
    void shouldNotPlaceVerticalShipOutsideBoardBoundary() {
        Board board = new Board(10, 10);
        Ship ship = new Ship("Battleship", 4);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship, new Coordinate(5, 8), Orientation.VERTICAL)
        );

        assertEquals("Ship cannot be placed outside the board!", exception.getMessage());
    }

    @Test
    void shouldNotPlaceHorizontalShipOverlappingAnother() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Cruiser", 3);
        Ship ship2 = new Ship("Destroyer", 2);

        board.placeShip(ship1, new Coordinate(2, 2), Orientation.HORIZONTAL);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship2, new Coordinate(3, 2), Orientation.HORIZONTAL)
        );

        assertEquals("Cannot place a ship on top of another ship!", exception.getMessage());
    }

    @Test
    void shouldNotPlaceVerticalShipOverlappingAnother() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Battleship", 4);
        Ship ship2 = new Ship("Destroyer", 2);

        board.placeShip(ship1, new Coordinate(3, 3), Orientation.VERTICAL);

        InvalidMoveException exception = assertThrows(
                InvalidMoveException.class,
                () -> board.placeShip(ship2, new Coordinate(3, 4), Orientation.VERTICAL)
        );

        assertEquals("Cannot place a ship on top of another ship!", exception.getMessage());
    }

    @Test
    void shouldPlaceMultipleShipsWithDifferentOrientations() {
        Board board = new Board(10, 10);
        Ship ship1 = new Ship("Cruiser", 3);
        Ship ship2 = new Ship("Battleship", 4);
        Ship ship3 = new Ship("Destroyer", 2);

        board.placeShip(ship1, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(ship2, new Coordinate(5, 5), Orientation.VERTICAL);
        board.placeShip(ship3, new Coordinate(8, 8), Orientation.HORIZONTAL);

        assertEquals(AttackResult.HIT, board.attack(new Coordinate(0, 0)));
        assertEquals(AttackResult.HIT, board.attack(new Coordinate(1, 0)));
        assertEquals(AttackResult.DESTROYED, board.attack(new Coordinate(2, 0)));

        assertEquals(AttackResult.HIT, board.attack(new Coordinate(5, 5)));
        assertEquals(AttackResult.HIT, board.attack(new Coordinate(5, 6)));

        Assertions.assertFalse(board.allShipsDestroyed());
    }

}
