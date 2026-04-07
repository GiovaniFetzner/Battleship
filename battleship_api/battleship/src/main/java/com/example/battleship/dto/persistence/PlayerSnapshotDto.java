package com.example.battleship.dto.persistence;

import java.util.UUID;

public class PlayerSnapshotDto {

    private UUID id;
    private String name;
    private boolean ready;
    private int seatNumber;
    private BoardSnapshotDto board;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BoardSnapshotDto getBoard() {
        return board;
    }

    public void setBoard(BoardSnapshotDto board) {
        this.board = board;
    }
}
