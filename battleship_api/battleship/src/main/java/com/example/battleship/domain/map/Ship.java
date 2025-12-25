package com.example.battleship.domain.map;

public class Ship {

    private String name;
    private final int size;
    private int hits = 0;
    private boolean destroyed = false;


    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public Ship(int size) {
        this.size = size;
    }

    public void hit(){
        if (destroyed) return;

        hits++;

        if (hits == size) {
            destroyed = true;
        }
    }

    boolean isDestroyed(){
        return  destroyed;
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
