package com.example.battleship.dto.rest.outbound;

public class CellResponse {

    private int x;
    private int y;
    private boolean attacked;
    private boolean hit;
    private boolean hasShip;

    public CellResponse() {}
    
    public CellResponse(int x, int y, boolean attacked, boolean hit, boolean hasShip) {
        this.x = x;
        this.y = y;
        this.attacked = attacked;
        this.hit = hit;
        this.hasShip = hasShip;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isAttacked() {
        return attacked;
    }

    public void setAttacked(boolean attacked) {
        this.attacked = attacked;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isHasShip() {
        return hasShip;
    }

    public void setHasShip(boolean hasShip) {
        this.hasShip = hasShip;
    }

}
