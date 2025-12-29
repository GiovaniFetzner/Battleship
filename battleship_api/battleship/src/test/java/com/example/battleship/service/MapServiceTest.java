package com.example.battleship.service;

import com.example.battleship.domain.map.Orientation;
import com.example.battleship.service.impl.MapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapServiceTest {

    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapService = new MapServiceImpl();
    }

    @Test
    void shouldValidateValidCoordinate() {
        assertTrue(mapService.isValidCoordinate(0, 0, 10, 10));
        assertTrue(mapService.isValidCoordinate(5, 5, 10, 10));
        assertTrue(mapService.isValidCoordinate(9, 9, 10, 10));
    }

    @Test
    void shouldInvalidateOutOfBoundsCoordinate() {
        assertFalse(mapService.isValidCoordinate(-1, 0, 10, 10));
        assertFalse(mapService.isValidCoordinate(0, -1, 10, 10));
        assertFalse(mapService.isValidCoordinate(10, 0, 10, 10));
        assertFalse(mapService.isValidCoordinate(0, 10, 10, 10));
    }

    @Test
    void shouldValidateHorizontalShipPlacement() {
        assertTrue(mapService.canPlaceShip(0, 0, 3, Orientation.HORIZONTAL, 10, 10));
        assertTrue(mapService.canPlaceShip(7, 5, 3, Orientation.HORIZONTAL, 10, 10));
    }

    @Test
    void shouldInvalidateHorizontalShipOutOfBounds() {
        assertFalse(mapService.canPlaceShip(8, 0, 3, Orientation.HORIZONTAL, 10, 10));
        assertFalse(mapService.canPlaceShip(9, 0, 2, Orientation.HORIZONTAL, 10, 10));
    }

    @Test
    void shouldValidateVerticalShipPlacement() {
        assertTrue(mapService.canPlaceShip(0, 0, 3, Orientation.VERTICAL, 10, 10));
        assertTrue(mapService.canPlaceShip(5, 7, 3, Orientation.VERTICAL, 10, 10));
    }

    @Test
    void shouldInvalidateVerticalShipOutOfBounds() {
        assertFalse(mapService.canPlaceShip(0, 8, 3, Orientation.VERTICAL, 10, 10));
        assertFalse(mapService.canPlaceShip(0, 9, 2, Orientation.VERTICAL, 10, 10));
    }
}

