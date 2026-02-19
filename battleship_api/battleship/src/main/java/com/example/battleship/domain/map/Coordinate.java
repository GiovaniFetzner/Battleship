package com.example.battleship.domain.map;

public record Coordinate(int x, int y) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return x == that.x && y == that.y;
    }

}
