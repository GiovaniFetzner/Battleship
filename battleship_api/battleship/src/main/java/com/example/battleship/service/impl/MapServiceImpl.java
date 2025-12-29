package com.example.battleship.service.impl;

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
}
