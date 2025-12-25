package com.example.battleship.domain.game;

import java.util.Objects;

public class Turn {
    private final Player player;
    private final int turnNumber;

    public Turn(Player player, int turnNumber) {
        this.player = player;
        this.turnNumber = turnNumber;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turn turn = (Turn) o;
        return turnNumber == turn.turnNumber && Objects.equals(player, turn.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, turnNumber);
    }

    @Override
    public String toString() {
        return "Turn{" +
                "player=" + player +
                ", turnNumber=" + turnNumber +
                '}';
    }
}
