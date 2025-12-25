package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;

import java.util.Arrays;

public class Board {

    private final int width;
    private final int height;
    private final Cell[][] cells;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        initiateCells(width, height);
    }

    private void initiateCells(int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell();
            }
        }
    }

    protected void placeShip(Ship ship, Coordinate coordinate) {
        positionValidation(coordinate, "Ship cannot be placed outside the board!");

        Cell cell = cells[coordinate.getX()][coordinate.getY()];
        if (cell.hasShip()) {
            throw new InvalidMoveException("Cannot place a ship on top of another ship!");
        }

        cell.placeShip(ship);
    }

    private void positionValidation(Coordinate coordinate, String message) {
        if (coordinate.getX() < 0 || coordinate.getX() >= width ||
                coordinate.getY() < 0 || coordinate.getY() >= height) {
            throw new InvalidMoveException(message);
        }
    }

    public AttackResult attack(Coordinate coordinate) {
        positionValidation(coordinate, "Attack outside board, please review the coordinates!");

        Cell cell = cells[coordinate.getX()][coordinate.getY()];
        if (cell.hasShip()) {
            return cell.attack();
        }

        return AttackResult.MISS;
    }

    public boolean allShipsDestroyed() {
        return Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .noneMatch(Cell::hasShip);
    }

    public void destroyAllShips() {
        Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .filter(Cell::hasShip)
                .forEach(cell -> cell.attack());
    }
}
