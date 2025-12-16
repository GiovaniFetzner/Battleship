package com.example.battleship.domain.map;

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

    public AttackResult attack(Coordinate coordinate){
        Cell cell = cells[coordinate.getX()][coordinate.getY()];
        return cell.attack();
    }

}
