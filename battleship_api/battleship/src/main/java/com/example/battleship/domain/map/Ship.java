package com.example.battleship.domain.map;

public class Ship {

    private final String name;
    private final int size;
    private int hits = 0;

    public Ship(String name, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Ship size must be greater than zero");
        }
        this.name = name;
        this.size = size;
    }

    public void hit() {
        if (isDestroyed()) {
            return;
        }
        hits++;
    }

    public boolean isDestroyed() {
        return hits >= size;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public int getHits() {
        return hits;
    }
}
