package com.example.battleship.dto.rest.outbound;

public class ShipStatusResponse {

    private String name;
    private int size;
    private int hits;
    private boolean destroyed;
    private boolean placed;

    public ShipStatusResponse() {
    }

    public ShipStatusResponse(String name, int size, int hits, boolean destroyed, boolean placed) {
        this.name = name;
        this.size = size;
        this.hits = hits;
        this.destroyed = destroyed;
        this.placed = placed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }
}
