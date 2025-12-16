package com.example.battleship.domain.map;

public class Cell {

    private Coordinate coordinate;

    private Ship ship;

    private boolean attacked = false;

    public Cell(Coordinate coordinate, Ship ship, boolean attacked) {
        this.coordinate = coordinate;
        this.ship = ship;
        this.attacked = attacked;
    }

    public Cell(){

    }

    public  AttackResult attack(){
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
}
