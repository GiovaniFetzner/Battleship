package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Board;
import com.example.battleship.domain.map.Ship;

import java.util.List;

public class Player {

    private final String id;
    private final Board board;
    private List<Ship> ships;
    private boolean hasLost;

    public Player(String name) {
        this.id = name;
        this.board = new Board(10, 10);
        this.ships = List.of();
        this.hasLost = false;
    }

    public boolean hasLost() {
        return hasLost;
    }

    public void loseAllShips() {
        this.hasLost = true;
        for (Ship ship : ships) {
            ship.setHits(ship.getSize());
        }
    }

    public List<Ship> getShips() {
        return ships;
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public Board getBoard() {
        return board;
    }

    public String getId() {
        return this.id;
    }

    public int getAliveShipsCount() {
        if (ships == null) return 0;
        return (int) ships.stream().filter(ship -> ship.getHits() < ship.getSize()).count();
    }
}