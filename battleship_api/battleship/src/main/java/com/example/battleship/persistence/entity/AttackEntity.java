package com.example.battleship.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attacks")
public class AttackEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacker_player_id", nullable = false)
    private PlayerEntity attacker;

    @Column(name = "target_x", nullable = false)
    private int targetX;

    @Column(name = "target_y", nullable = false)
    private int targetY;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private AttackResultType result;

    @Column(name = "attacked_at", nullable = false)
    private Instant attackedAt;

    protected AttackEntity() {
    }

    public static AttackEntity createNew() {
        AttackEntity attackEntity = new AttackEntity();
        attackEntity.id = UUID.randomUUID();
        return attackEntity;
    }

    public UUID getId() {
        return id;
    }

    public void assignId(UUID id) {
        this.id = id;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public PlayerEntity getAttacker() {
        return attacker;
    }

    public void setAttacker(PlayerEntity attacker) {
        this.attacker = attacker;
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
