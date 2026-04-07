package com.example.battleship.dto.persistence;

import java.util.HashSet;
import java.util.Set;

public class BoardSnapshotDto {

    private int width;
    private int height;
    private Set<CoordinateSnapshotDto> shipCells = new HashSet<>();
    private Set<CoordinateSnapshotDto> hitCells = new HashSet<>();
    private Set<CoordinateSnapshotDto> missCells = new HashSet<>();

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Set<CoordinateSnapshotDto> getShipCells() {
        return shipCells;
    }

    public void setShipCells(Set<CoordinateSnapshotDto> shipCells) {
        this.shipCells = shipCells;
    }

    public Set<CoordinateSnapshotDto> getHitCells() {
        return hitCells;
    }

    public void setHitCells(Set<CoordinateSnapshotDto> hitCells) {
        this.hitCells = hitCells;
    }

    public Set<CoordinateSnapshotDto> getMissCells() {
        return missCells;
    }

    public void setMissCells(Set<CoordinateSnapshotDto> missCells) {
        this.missCells = missCells;
    }
}
