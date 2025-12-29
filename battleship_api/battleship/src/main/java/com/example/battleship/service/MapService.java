package com.example.battleship.service;

import com.example.battleship.domain.map.Coordinate;
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

    /**
     * Converte uma string de coordenada (ex: "A5") para objeto Coordinate
     */
    Coordinate parseCoordinate(String coordinateStr);

    /**
     * Converte um objeto Coordinate para string (ex: "A5")
     */
    String formatCoordinate(Coordinate coordinate);
}
