package com.example.battleship.service.impl;

import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.service.MapService;
import org.springframework.stereotype.Service;

@Service
public class MapServiceImpl implements MapService {

    @Override
    public boolean isValidCoordinate(int x, int y, int boardWidth, int boardHeight) {
        return x >= 0 && x < boardWidth && y >= 0 && y < boardHeight;
    }

    @Override
    public boolean canPlaceShip(int x, int y, int shipSize, Orientation orientation,
                               int boardWidth, int boardHeight) {
        if (!isValidCoordinate(x, y, boardWidth, boardHeight)) {
            return false;
        }

        if (orientation == Orientation.HORIZONTAL) {
            return (x + shipSize) <= boardWidth;
        } else {
            return (y + shipSize) <= boardHeight;
        }
    }

    @Override
    public Coordinate parseCoordinate(String coordinateStr) {
        if (coordinateStr == null || coordinateStr.length() < 2) {
            throw new IllegalArgumentException("Invalid coordinate format");
        }

        // Formato esperado: "A5" -> x=0, y=4
        char col = coordinateStr.charAt(0);
        int row = Integer.parseInt(coordinateStr.substring(1)) - 1;

        int x = Character.toUpperCase(col) - 'A';

        return new Coordinate(x, row);
    }

    @Override
    public String formatCoordinate(Coordinate coordinate) {
        char col = (char) ('A' + coordinate.getX());
        int row = coordinate.getY() + 1;

        return col + String.valueOf(row);
    }
}
