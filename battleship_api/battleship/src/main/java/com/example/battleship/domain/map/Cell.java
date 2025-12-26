package com.example.battleship.domain.map;

import com.example.battleship.exception.InvalidMoveException;

public class Cell {

    private Coordinate coordinate;

    private Ship ship;

    private boolean attacked = false;

    public Cell(){
    }

    public  AttackResult attack(){

        if (attacked){
            throw new InvalidMoveException("Cell already attacked!");
        }

        this.attacked = true;

        if(ship == null){
            return AttackResult.MISS;
        }

        ship.hit();
        return ship.isDestroyed() ? AttackResult.DESTROYED : AttackResult.HIT;
    }

    public boolean isAttacked() {
        return attacked;
    }

    void placeShip (Ship ship){
        this.ship = ship;
    }

    public boolean hasShip() {
        return ship != null;
    }

    public boolean hasAliveShip() {
        return ship != null && !ship.isDestroyed();
    }
}
