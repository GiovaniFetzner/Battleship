package com.example.battleship.dto.rest.outbound;

public class AttackResponse {
    private int x;
    private int y;
    private boolean hit;
    private boolean destroyed;

    public AttackResponse() {}

    public AttackResponse(int x, int y, boolean hit, boolean destroyed) {
        this.x = x;
        this.y = y;
        this.hit = hit;
        this.destroyed = destroyed;
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

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
