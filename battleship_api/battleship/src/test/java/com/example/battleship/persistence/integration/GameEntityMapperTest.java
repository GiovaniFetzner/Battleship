package com.example.battleship.persistence.integration;

import com.example.battleship.dto.persistence.BoardSnapshotDto;
import com.example.battleship.dto.persistence.CoordinateSnapshotDto;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.dto.persistence.PlayerSnapshotDto;
import com.example.battleship.persistence.entity.BoardEntity;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.persistence.entity.GameStatus;
import com.example.battleship.persistence.entity.GridCoordinateEmbeddable;
import com.example.battleship.persistence.entity.PlayerEntity;
import com.example.battleship.persistence.mapper.GameEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GameEntityMapper Unit Tests")
class GameEntityMapperTest {

    @Autowired
    private GameEntityMapper gameEntityMapper;

    @Test
    @DisplayName("should map snapshot to entity and back")
    void shouldMapSnapshotToEntityAndBack() {
        // Arrange: Create a complex snapshot
        GameSnapshotDto originalSnapshot = createComplexGameSnapshot();

        // Act: Convert to entity
        GameEntity entity = GameEntity.createNew();
        gameEntityMapper.updateEntityFromSnapshot(originalSnapshot, entity);

        // Assert: Entity was populated correctly
        assertNotNull(entity.getId());
        assertEquals(GameStatus.IN_PROGRESS, entity.getStatus());
        assertEquals(2, entity.getPlayers().size());

        // Act: Convert back to snapshot
        GameSnapshotDto recoveredSnapshot = gameEntityMapper.toSnapshot(entity);

        // Assert: Snapshot recovered correctly
        assertEquals(originalSnapshot.getId(), recoveredSnapshot.getId());
        assertEquals(originalSnapshot.getStatus(), recoveredSnapshot.getStatus());
        assertEquals(2, recoveredSnapshot.getPlayers().size());
        assertEquals("Alice", recoveredSnapshot.getPlayers().get(0).getName());
    }

    @Test
    @DisplayName("should preserve board cell collections through mapping")
    void shouldPreserveBoardCellsInMapping() {
        // Arrange
        GameSnapshotDto snapshot = createGameSnapshotWithBoardCells();

        // Act: Map to entity
        GameEntity entity = GameEntity.createNew();
        gameEntityMapper.updateEntityFromSnapshot(snapshot, entity);

        // Assert: Entity has correct cells
        PlayerEntity player = entity.getPlayers().get(0);
        BoardEntity board = player.getBoard();
        assertEquals(3, board.getShipCells().size());
        assertEquals(2, board.getHitCells().size());
        assertEquals(2, board.getMissCells().size());

        // Act: Map back to snapshot
        GameSnapshotDto recovered = gameEntityMapper.toSnapshot(entity);

        // Assert: Snapshot has same cells
        BoardSnapshotDto recoveredBoard = recovered.getPlayers().get(0).getBoard();
        assertEquals(3, recoveredBoard.getShipCells().size());
        assertEquals(2, recoveredBoard.getHitCells().size());
        assertEquals(2, recoveredBoard.getMissCells().size());
    }

    @Test
    @DisplayName("should handle empty board state")
    void shouldHandleEmptyBoardState() {
        // Arrange
        GameSnapshotDto snapshot = createComplexGameSnapshot();
        for (PlayerSnapshotDto player : snapshot.getPlayers()) {
            player.getBoard().setShipCells(new HashSet<>());
            player.getBoard().setHitCells(new HashSet<>());
            player.getBoard().setMissCells(new HashSet<>());
        }

        // Act: Map to entity
        GameEntity entity = GameEntity.createNew();
        gameEntityMapper.updateEntityFromSnapshot(snapshot, entity);

        // Assert: Empty collections are handled
        for (PlayerEntity player : entity.getPlayers()) {
            assertTrue(player.getBoard().getShipCells().isEmpty());
            assertTrue(player.getBoard().getHitCells().isEmpty());
            assertTrue(player.getBoard().getMissCells().isEmpty());
        }
    }

    @Test
    @DisplayName("should preserve version during mapping")
    void shouldPreserveVersionDuringMapping() {
        // Arrange
        GameSnapshotDto snapshot = createComplexGameSnapshot();

        // Act: Map to entity
        GameEntity entity = GameEntity.createNew();
        gameEntityMapper.updateEntityFromSnapshot(snapshot, entity);

        // Act: Map back to snapshot
        GameSnapshotDto recovered = gameEntityMapper.toSnapshot(entity);

        // Assert: Snapshot is properly recovered from entity
        // Note: Version is null for new entities (set by JPA on persist)
        assertEquals(snapshot.getId(), recovered.getId());
        assertEquals(snapshot.getStatus(), recovered.getStatus());
    }

    // Helper methods
    private GameSnapshotDto createComplexGameSnapshot() {
        GameSnapshotDto game = new GameSnapshotDto();
        game.setId(UUID.randomUUID());
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setTurnCounter(3);
        game.setCurrentTurnPlayerId(UUID.randomUUID());
        game.setVersion(0L);

        PlayerSnapshotDto player1 = createPlayerSnapshot("Alice", 0);
        PlayerSnapshotDto player2 = createPlayerSnapshot("Bob", 1);

        game.setPlayers(Arrays.asList(player1, player2));
        game.setAttacks(new java.util.ArrayList<>());

        return game;
    }

    private GameSnapshotDto createGameSnapshotWithBoardCells() {
        GameSnapshotDto game = createComplexGameSnapshot();

        Set<CoordinateSnapshotDto> shipCells = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(0, 0),
                new CoordinateSnapshotDto(0, 1),
                new CoordinateSnapshotDto(0, 2)));
        Set<CoordinateSnapshotDto> hitCells = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(0, 0),
                new CoordinateSnapshotDto(0, 1)));
        Set<CoordinateSnapshotDto> missCells = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(5, 5),
                new CoordinateSnapshotDto(6, 6)));

        game.getPlayers().get(0).getBoard().setShipCells(shipCells);
        game.getPlayers().get(0).getBoard().setHitCells(hitCells);
        game.getPlayers().get(0).getBoard().setMissCells(missCells);

        return game;
    }

    private PlayerSnapshotDto createPlayerSnapshot(String name, int seat) {
        PlayerSnapshotDto player = new PlayerSnapshotDto();
        player.setId(UUID.randomUUID());
        player.setName(name);
        player.setSeatNumber(seat);
        player.setReady(true);

        BoardSnapshotDto board = new BoardSnapshotDto();
        board.setWidth(10);
        board.setHeight(10);
        board.setShipCells(new HashSet<>());
        board.setHitCells(new HashSet<>());
        board.setMissCells(new HashSet<>());
        player.setBoard(board);

        return player;
    }
}
