package com.example.battleship.domain.map;

public class Ship {

    private final int size;

    private int hits = 0;

    public Ship(int size) {
        this.size = size;
    }

    void hit(){
        hits++;
    }

    boolean isDestroyed(){
        return  hits == size;
    }

}
