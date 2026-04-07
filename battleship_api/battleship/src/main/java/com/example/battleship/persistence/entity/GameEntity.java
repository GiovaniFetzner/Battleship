package com.example.battleship.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Column(name = "current_turn_player_id")
    private UUID currentTurnPlayerId;

    @Column(name = "winner_player_id")
    private UUID winnerPlayerId;

    @Column(name = "turn_counter", nullable = false)
    private int turnCounter;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @JsonIgnore
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerEntity> players = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttackEntity> attacks = new ArrayList<>();

    protected GameEntity() {
    }

    public static GameEntity createNew() {
        GameEntity gameEntity = new GameEntity();
        gameEntity.id = UUID.randomUUID();
        gameEntity.status = GameStatus.WAITING_FOR_PLAYERS;
        gameEntity.turnCounter = 0;
        return gameEntity;
    }

    public UUID getId() {
        return id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public UUID getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(UUID currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public UUID getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public void setWinnerPlayerId(UUID winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public void setTurnCounter(int turnCounter) {
        this.turnCounter = turnCounter;
    }

    public Long getVersion() {
        return version;
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }

    public List<AttackEntity> getAttacks() {
        return attacks;
    }

    public void addPlayer(PlayerEntity player) {
        players.add(player);
        player.setGame(this);
    }

    public void clearPlayers() {
        for (PlayerEntity player : players) {
            player.setGame(null);
        }
        players.clear();
    }

    public void addAttack(AttackEntity attack) {
        attacks.add(attack);
        attack.setGame(this);
    }

    public void clearAttacks() {
        for (AttackEntity attack : attacks) {
            attack.setGame(null);
        }
        attacks.clear();
    }

    public void assignId(UUID id) {
        this.id = id;
    }
}
