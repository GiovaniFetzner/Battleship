package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;

public class Cell {

    private Ship ship;
    private boolean attacked = false;

    public AttackResult attack() {

        if (attacked) {
            throw new InvalidMoveException("Cell already attacked!");
        }

        attacked = true;

        if (ship == null) {
            return AttackResult.MISS;
        }

        ship.hit();

        return ship.isDestroyed()
                ? AttackResult.DESTROYED
                : AttackResult.HIT;
    }

    void placeShip(Ship ship) {

        if (this.ship != null) {
            throw new InvalidMoveException("Cell already contains a ship!");
        }

        this.ship = ship;
    }

    public boolean isAttacked() {
        return attacked;
    }

    public boolean hasShip() {
        return ship != null;
    }

    public boolean hasAliveShip() {
        return ship != null && !ship.isDestroyed();
    }
}
