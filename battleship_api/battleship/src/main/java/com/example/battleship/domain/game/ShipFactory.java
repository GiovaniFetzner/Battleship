package com.example.battleship.domain.game;

import com.example.battleship.domain.map.Ship;

import java.util.ArrayList;
import java.util.List;

public class ShipFactory {

    public static List<Ship> createDefaultShips() {
        List<Ship> ships = new ArrayList<>();
        ships.add(new Ship("Porta-Aviões", 5));
        ships.add(new Ship("Bombardeiro", 4));
        ships.add(new Ship("Submarino", 3));
        ships.add(new Ship("Lancha Militar", 2));
        return ships;
    }
}

