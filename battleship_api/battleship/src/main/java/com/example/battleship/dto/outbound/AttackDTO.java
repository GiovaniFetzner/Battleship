package com.example.battleship.dto.outbound;

public class AttackDTO {
    private int x;
    private int y;
    private String result; // "HIT", "MISS", "DESTROYED"

    public AttackDTO() {}

    public AttackDTO(int x, int y, String result) {
        this.x = x;
        this.y = y;
        this.result = result;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}