package com.example.battleship.dto.persistence;

import com.example.battleship.persistence.entity.GameStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameSnapshotDto {

    private UUID id;
    private GameStatus status;
    private UUID currentTurnPlayerId;
    private UUID winnerPlayerId;
    private int turnCounter;
    private Long version;
    private List<PlayerSnapshotDto> players = new ArrayList<>();
    private List<AttackSnapshotDto> attacks = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<PlayerSnapshotDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerSnapshotDto> players) {
        this.players = players;
    }

    public List<AttackSnapshotDto> getAttacks() {
        return attacks;
    }

    public void setAttacks(List<AttackSnapshotDto> attacks) {
        this.attacks = attacks;
    }
}
