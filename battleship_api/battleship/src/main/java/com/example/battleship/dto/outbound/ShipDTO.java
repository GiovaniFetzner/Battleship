package com.example.battleship.dto.outbound;

public class ShipDTO {
    private String name;
    private int size;
    private int hits;
    private boolean destroyed;

    public ShipDTO() {}

    public ShipDTO(String name, int size, int hits, boolean destroyed) {
        this.name = name;
        this.size = size;
        this.hits = hits;
        this.destroyed = destroyed;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }

    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
}