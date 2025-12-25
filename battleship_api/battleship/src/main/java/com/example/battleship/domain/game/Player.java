package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Board;
import com.example.battleship.domain.map.Ship;

import java.util.List;

public class Player {

    private final String name;
    private final Board board;
    private List<Ship> ships;
    private boolean lostAllShips;

    public Player(String name) {
        this.name = name;
        this.board = new Board(10, 10);
        this.ships = List.of();
        this.lostAllShips = false;
    }

    public boolean hasLost() {
        return lostAllShips;
    }

    public void loseAllShips() {
        this.lostAllShips = true;
        for (Ship ship : ships) {
            while (ship.getHits() < ship.getSize()){
                ship.hit();
            }
        }
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }
}
