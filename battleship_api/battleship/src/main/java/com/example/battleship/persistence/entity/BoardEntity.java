package com.example.battleship.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "boards")
public class BoardEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerEntity player;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;

    @ElementCollection
    @CollectionTable(name = "board_ship_cells", joinColumns = @JoinColumn(name = "board_id"))
    private Set<GridCoordinateEmbeddable> shipCells = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "board_hit_cells", joinColumns = @JoinColumn(name = "board_id"))
    private Set<GridCoordinateEmbeddable> hitCells = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "board_miss_cells", joinColumns = @JoinColumn(name = "board_id"))
    private Set<GridCoordinateEmbeddable> missCells = new HashSet<>();

    protected BoardEntity() {
    }

    public static BoardEntity createNew() {
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.id = UUID.randomUUID();
        return boardEntity;
    }

    public UUID getId() {
        return id;
    }

    public void assignId(UUID id) {
        this.id = id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Set<GridCoordinateEmbeddable> getShipCells() {
        return shipCells;
    }

    public Set<GridCoordinateEmbeddable> getHitCells() {
        return hitCells;
    }

    public Set<GridCoordinateEmbeddable> getMissCells() {
        return missCells;
    }

    public void replaceShipCells(Set<GridCoordinateEmbeddable> coordinates) {
        shipCells.clear();
        shipCells.addAll(coordinates);
    }

    public void replaceHitCells(Set<GridCoordinateEmbeddable> coordinates) {
        hitCells.clear();
        hitCells.addAll(coordinates);
    }

    public void replaceMissCells(Set<GridCoordinateEmbeddable> coordinates) {
        missCells.clear();
        missCells.addAll(coordinates);
    }
}
