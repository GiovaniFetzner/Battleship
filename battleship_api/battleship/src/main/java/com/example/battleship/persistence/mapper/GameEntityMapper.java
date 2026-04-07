package com.example.battleship.persistence.mapper;

import com.example.battleship.dto.persistence.AttackSnapshotDto;
import com.example.battleship.dto.persistence.BoardSnapshotDto;
import com.example.battleship.dto.persistence.CoordinateSnapshotDto;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.dto.persistence.PlayerSnapshotDto;
import com.example.battleship.persistence.entity.AttackEntity;
import com.example.battleship.persistence.entity.BoardEntity;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.persistence.entity.GridCoordinateEmbeddable;
import com.example.battleship.persistence.entity.PlayerEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GameEntityMapper {

    public void updateEntityFromSnapshot(GameSnapshotDto snapshot, GameEntity gameEntity) {
        if (snapshot.getId() != null) {
            gameEntity.assignId(snapshot.getId());
        }

        gameEntity.setStatus(snapshot.getStatus());
        gameEntity.setCurrentTurnPlayerId(snapshot.getCurrentTurnPlayerId());
        gameEntity.setWinnerPlayerId(snapshot.getWinnerPlayerId());
        gameEntity.setTurnCounter(snapshot.getTurnCounter());

        gameEntity.clearPlayers();
        for (PlayerSnapshotDto playerSnapshot : snapshot.getPlayers()) {
            PlayerEntity playerEntity = PlayerEntity.createNew();
            if (playerSnapshot.getId() != null) {
                playerEntity.assignId(playerSnapshot.getId());
            }
            playerEntity.setName(playerSnapshot.getName());
            playerEntity.setReady(playerSnapshot.isReady());
            playerEntity.setSeatNumber(playerSnapshot.getSeatNumber());

            BoardSnapshotDto boardSnapshot = playerSnapshot.getBoard();
            if (boardSnapshot != null) {
                BoardEntity boardEntity = BoardEntity.createNew();
                boardEntity.setWidth(boardSnapshot.getWidth());
                boardEntity.setHeight(boardSnapshot.getHeight());
                boardEntity.replaceShipCells(toEmbeddableSet(boardSnapshot.getShipCells()));
                boardEntity.replaceHitCells(toEmbeddableSet(boardSnapshot.getHitCells()));
                boardEntity.replaceMissCells(toEmbeddableSet(boardSnapshot.getMissCells()));
                playerEntity.setBoard(boardEntity);
            }

            gameEntity.addPlayer(playerEntity);
        }

        Map<UUID, PlayerEntity> playerById = new HashMap<>();
        for (PlayerEntity playerEntity : gameEntity.getPlayers()) {
            playerById.put(playerEntity.getId(), playerEntity);
        }

        gameEntity.clearAttacks();
        for (AttackSnapshotDto attackSnapshot : snapshot.getAttacks()) {
            PlayerEntity attacker = playerById.get(attackSnapshot.getAttackerPlayerId());
            if (attacker == null) {
                throw new IllegalArgumentException("Attacker not found in game players: " + attackSnapshot.getAttackerPlayerId());
            }

            AttackEntity attackEntity = AttackEntity.createNew();
            if (attackSnapshot.getId() != null) {
                attackEntity.assignId(attackSnapshot.getId());
            }
            attackEntity.setAttacker(attacker);
            attackEntity.setTargetX(attackSnapshot.getTargetX());
            attackEntity.setTargetY(attackSnapshot.getTargetY());
            attackEntity.setResult(attackSnapshot.getResult());
            attackEntity.setAttackedAt(attackSnapshot.getAttackedAt());

            gameEntity.addAttack(attackEntity);
        }
    }

    public GameSnapshotDto toSnapshot(GameEntity entity) {
        GameSnapshotDto snapshot = new GameSnapshotDto();
        snapshot.setId(entity.getId());
        snapshot.setStatus(entity.getStatus());
        snapshot.setCurrentTurnPlayerId(entity.getCurrentTurnPlayerId());
        snapshot.setWinnerPlayerId(entity.getWinnerPlayerId());
        snapshot.setTurnCounter(entity.getTurnCounter());
        snapshot.setVersion(entity.getVersion());

        for (PlayerEntity player : entity.getPlayers()) {
            PlayerSnapshotDto playerSnapshot = new PlayerSnapshotDto();
            playerSnapshot.setId(player.getId());
            playerSnapshot.setName(player.getName());
            playerSnapshot.setReady(player.isReady());
            playerSnapshot.setSeatNumber(player.getSeatNumber());

            BoardEntity board = player.getBoard();
            if (board != null) {
                BoardSnapshotDto boardSnapshot = new BoardSnapshotDto();
                boardSnapshot.setWidth(board.getWidth());
                boardSnapshot.setHeight(board.getHeight());
                boardSnapshot.setShipCells(toCoordinateSnapshotSet(board.getShipCells()));
                boardSnapshot.setHitCells(toCoordinateSnapshotSet(board.getHitCells()));
                boardSnapshot.setMissCells(toCoordinateSnapshotSet(board.getMissCells()));
                playerSnapshot.setBoard(boardSnapshot);
            }

            snapshot.getPlayers().add(playerSnapshot);
        }

        for (AttackEntity attack : entity.getAttacks()) {
            AttackSnapshotDto attackSnapshot = new AttackSnapshotDto();
            attackSnapshot.setId(attack.getId());
            attackSnapshot.setAttackerPlayerId(attack.getAttacker().getId());
            attackSnapshot.setTargetX(attack.getTargetX());
            attackSnapshot.setTargetY(attack.getTargetY());
            attackSnapshot.setResult(attack.getResult());
            attackSnapshot.setAttackedAt(attack.getAttackedAt());
            snapshot.getAttacks().add(attackSnapshot);
        }

        return snapshot;
    }

    private Set<GridCoordinateEmbeddable> toEmbeddableSet(Set<CoordinateSnapshotDto> coordinates) {
        Set<GridCoordinateEmbeddable> embeddables = new HashSet<>();
        if (coordinates == null) {
            return embeddables;
        }

        for (CoordinateSnapshotDto coordinate : coordinates) {
            embeddables.add(new GridCoordinateEmbeddable(coordinate.x(), coordinate.y()));
        }
        return embeddables;
    }

    private Set<CoordinateSnapshotDto> toCoordinateSnapshotSet(Set<GridCoordinateEmbeddable> coordinates) {
        Set<CoordinateSnapshotDto> snapshots = new HashSet<>();
        for (GridCoordinateEmbeddable coordinate : coordinates) {
            snapshots.add(new CoordinateSnapshotDto(coordinate.getX(), coordinate.getY()));
        }
        return snapshots;
    }
}
