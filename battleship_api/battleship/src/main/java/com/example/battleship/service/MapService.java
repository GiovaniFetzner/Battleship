package com.example.battleship.service;

import com.example.battleship.domain.map.Orientation;

public interface MapService {

    /**
     * Valida se uma coordenada está dentro dos limites do tabuleiro
     */
    boolean isValidCoordinate(int x, int y, int boardWidth, int boardHeight);

    /**
     * Valida se um navio pode ser posicionado em uma coordenada específica
     */
    boolean canPlaceShip(int x, int y, int shipSize, Orientation orientation,
                        int boardWidth, int boardHeight);
}
