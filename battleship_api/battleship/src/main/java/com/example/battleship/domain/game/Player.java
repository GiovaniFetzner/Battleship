package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Board;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.exception.InvalidMoveException;

public class Player {

    private final String name;
    private final Board board;
    private boolean shipsPlaced = false;

    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank");
        }
        this.name = name;
        this.board = new Board(10, 10);
    }

    /* =============================
       SHIP PLACEMENT
       ============================= */

    public void placeShip(Ship ship, Coordinate coordinate, Orientation orientation) {

        if (shipsPlaced) {
            throw new InvalidMoveException("Ships already placed");
        }

        board.placeShip(ship, coordinate, orientation);
    }

    public void confirmShipsPlacement() {

        if (!board.hasRequiredShipsPlaced()) {
            throw new InvalidMoveException("Not all required ships were placed");
        }

        this.shipsPlaced = true;
    }

    public boolean hasPlacedShips() {
        return shipsPlaced;
    }

    /* =============================
       GAME STATUS
       ============================= */

    public boolean hasLost() {
        return board.allShipsDestroyed();
    }

    public Board getBoard() {
        return board;
    }

    public String getName() {
        return name;
    }
}
