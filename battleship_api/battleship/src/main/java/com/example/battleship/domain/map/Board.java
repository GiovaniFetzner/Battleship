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

    public void placeShip(Ship ship, Coordinate coordinate, Orientation orientation) {
        positionValidation(coordinate, "Ship cannot be placed outside the board!");

        // HORIZONTAL: Y fixo, X varia (incrementa) -> valida se X + size cabe
        // VERTICAL: X fixo, Y varia (incrementa) -> valida se Y + size cabe
        if (orientation == Orientation.HORIZONTAL) {
            if ((coordinate.getX() + ship.getSize()) > width) {
                throw new InvalidMoveException("Ship cannot be placed outside the board!");
            }
        } else { // VERTICAL
            if ((coordinate.getY() + ship.getSize()) > height) {
                throw new InvalidMoveException("Ship cannot be placed outside the board!");
            }
        }

        // Verificar se todas as células necessárias estão livres
        for (int i = 0; i < ship.getSize(); i++) {
            int x = orientation == Orientation.HORIZONTAL ? coordinate.getX() + i : coordinate.getX();
            int y = orientation == Orientation.HORIZONTAL ? coordinate.getY() : coordinate.getY() + i;

            if (cells[x][y].hasShip()) {
                throw new InvalidMoveException("Cannot place a ship on top of another ship!");
            }
        }

        // Colocar o navio em todas as células necessárias
        for (int i = 0; i < ship.getSize(); i++) {
            int x = orientation == Orientation.HORIZONTAL ? coordinate.getX() + i : coordinate.getX();
            int y = orientation == Orientation.HORIZONTAL ? coordinate.getY() : coordinate.getY() + i;

            cells[x][y].placeShip(ship);
        }
    }

    public void placeShip(Ship ship, Coordinate coordinate) {
        placeShip(ship, coordinate, Orientation.HORIZONTAL);
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
        if (cell.isAttacked()) {
            throw new InvalidMoveException("Cell already attacked!");
        }

        return cell.attack();
    }

    public boolean allShipsDestroyed() {
        return Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .noneMatch(Cell::hasAliveShip);
    }

}
