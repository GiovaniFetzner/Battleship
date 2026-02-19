package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int width;
    private final int height;
    private final Cell[][] cells;

    private final List<Ship> ships = new ArrayList<>();

    private static final int REQUIRED_SHIPS = 4;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        initiateCells();
    }

    private void initiateCells() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell();
            }
        }
    }

    /* =============================
       SHIP PLACEMENT
       ============================= */

    public void placeShip(Ship ship, Coordinate coordinate, Orientation orientation) {

        validatePosition(coordinate);

        if (ships.size() >= REQUIRED_SHIPS) {
            throw new InvalidMoveException("All ships already placed");
        }

        if (orientation == Orientation.HORIZONTAL) {
            if (coordinate.x() + ship.getSize() > width) {
                throw new InvalidMoveException("Ship cannot be placed outside the board!");
            }
        } else {
            if (coordinate.y() + ship.getSize() > height) {
                throw new InvalidMoveException("Ship cannot be placed outside the board!");
            }
        }

        // Verifica colisão
        for (int i = 0; i < ship.getSize(); i++) {

            int x = orientation == Orientation.HORIZONTAL ? coordinate.x() + i : coordinate.x();
            int y = orientation == Orientation.HORIZONTAL ? coordinate.y() : coordinate.y() + i;

            if (cells[x][y].hasShip()) {
                throw new InvalidMoveException("Cannot place a ship on top of another ship!");
            }
        }

        // Posiciona navio
        for (int i = 0; i < ship.getSize(); i++) {

            int x = orientation == Orientation.HORIZONTAL ? coordinate.x() + i : coordinate.x();
            int y = orientation == Orientation.HORIZONTAL ? coordinate.y() : coordinate.y() + i;

            cells[x][y].placeShip(ship);
        }

        ships.add(ship);
    }

    public boolean hasRequiredShipsPlaced() {
        return ships.size() == REQUIRED_SHIPS;
    }

    /* =============================
       BATTLE
       ============================= */

    public AttackResult attack(Coordinate coordinate) {

        validatePosition(coordinate);

        Cell cell = cells[coordinate.x()][coordinate.y()];

        if (cell.isAttacked()) {
            throw new InvalidMoveException("Cell already attacked!");
        }

        return cell.attack();
    }

    public boolean allShipsDestroyed() {

        if (ships.isEmpty()) {
            return false;
        }

        return ships.stream()
                .allMatch(Ship::isDestroyed);
    }

    /* =============================
       VALIDATION
       ============================= */

    private void validatePosition(Coordinate coordinate) {
        if (coordinate.x() < 0 || coordinate.x() >= width ||
                coordinate.y() < 0 || coordinate.y() >= height) {
            throw new InvalidMoveException("Position outside board!");
        }
    }
}
