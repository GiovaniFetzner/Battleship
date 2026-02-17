package com.example.battleship.dto.rest.outbound;

import java.util.List;

public class ShipResponse {
    private String shipId;
    private String type;
    private int size;
    private int hits;
    private List<CellResponse> coordinates;
    private boolean destroyed;

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<CellResponse> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<CellResponse> coordinates) {
        this.coordinates = coordinates;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }
}
