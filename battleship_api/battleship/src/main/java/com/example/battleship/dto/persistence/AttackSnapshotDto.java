package com.example.battleship.dto.persistence;

import com.example.battleship.persistence.entity.AttackResultType;

import java.time.Instant;
import java.util.UUID;

public class AttackSnapshotDto {

    private UUID id;
    private UUID attackerPlayerId;
    private int targetX;
    private int targetY;
    private AttackResultType result;
    private Instant attackedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAttackerPlayerId() {
        return attackerPlayerId;
    }

    public void setAttackerPlayerId(UUID attackerPlayerId) {
        this.attackerPlayerId = attackerPlayerId;
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public AttackResultType getResult() {
        return result;
    }

    public void setResult(AttackResultType result) {
        this.result = result;
    }

    public Instant getAttackedAt() {
        return attackedAt;
    }

    public void setAttackedAt(Instant attackedAt) {
        this.attackedAt = attackedAt;
    }
}
