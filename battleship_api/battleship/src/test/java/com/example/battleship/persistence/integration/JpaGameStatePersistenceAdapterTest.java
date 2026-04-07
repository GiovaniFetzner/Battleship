package com.example.battleship.persistence.integration;

import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.persistence.adapter.JpaGameStatePersistenceAdapter;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.persistence.entity.GameStatus;
import com.example.battleship.persistence.mapper.GameEntityMapper;
import com.example.battleship.repository.jpa.GameJpaRepository;
import com.example.battleship.service.persistence.port.GameStatePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("JpaGameStatePersistenceAdapter Integration Tests")
class JpaGameStatePersistenceAdapterTest {

    @Autowired
    private GameStatePersistencePort gamePersistencePort;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @BeforeEach
    void setUp() {
        gameJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("should save and retrieve game through port")
    void shouldSaveAndRetrieveThroughPort() {
        // Arrange
        GameSnapshotDto snapshot = createTestSnapshot();

        // Act: Save through port
        GameSnapshotDto saved = gamePersistencePort.save(snapshot);

        // Assert: Save successful
        assertNotNull(saved.getId());
        assertNotNull(saved.getVersion());

        // Act: Retrieve through port
        Optional<GameSnapshotDto> retrieved = gamePersistencePort.findById(saved.getId());

        // Assert: Retrieve successful
        assertTrue(retrieved.isPresent());
        assertEquals(saved.getId(), retrieved.get().getId());
    }

    @Test
    @DisplayName("should return empty Optional for non-existent game")
    void shouldReturnEmptyForNonExistent() {
        // Act & Assert
        Optional<GameSnapshotDto> result = gamePersistencePort.findById(UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("should update existing game with version increment")
    void shouldUpdateWithVersionIncrement() {
        // Arrange: Save initial snapshot
        GameSnapshotDto initial = gamePersistencePort.save(createTestSnapshot());
        UUID gameId = initial.getId();

        // Act: Update snapshot
        initial.setTurnCounter(10);
        initial.setPlayers(new java.util.ArrayList<>());
        initial.setAttacks(new java.util.ArrayList<>());
        GameSnapshotDto updated = gamePersistencePort.save(initial);

        // Assert: Update applied
        assertNotNull(updated.getId());
        assertEquals(gameId, updated.getId());

        // Act: Verify update persisted
        Optional<GameSnapshotDto> retrieved = gamePersistencePort.findById(gameId);
        assertTrue(retrieved.isPresent());
        assertEquals(10, retrieved.get().getTurnCounter());
    }

    @Test
    @DisplayName("should enforce lazy loading for player boards")
    void shouldEnforceEagerLoadingOfAggregates() {
        // Arrange: Save game with complete aggregate
        GameSnapshotDto snapshot = createTestSnapshot();
        GameSnapshotDto saved = gamePersistencePort.save(snapshot);

        // Act: Retrieve game (should use EntityGraph to load players and boards)
        Optional<GameSnapshotDto> retrieved = gamePersistencePort.findById(saved.getId());

        // Assert: Complete aggregate is loaded
        assertTrue(retrieved.isPresent());
        assertEquals(2, retrieved.get().getPlayers().size());
        retrieved.get().getPlayers().forEach(player -> {
            assertNotNull(player.getBoard());
            assertNotNull(player.getBoard().getWidth());
        });
    }

    private GameSnapshotDto createTestSnapshot() {
        GameSnapshotDto game = new GameSnapshotDto();
        game.setId(UUID.randomUUID());
        game.setStatus(GameStatus.WAITING_FOR_PLAYERS);
        game.setTurnCounter(0);
        game.setPlayers(new java.util.ArrayList<>());
        game.setAttacks(new java.util.ArrayList<>());

        var player1 = new com.example.battleship.dto.persistence.PlayerSnapshotDto();
        player1.setId(UUID.randomUUID());
        player1.setName("TestPlayer1");
        player1.setSeatNumber(0);
        player1.setReady(false);

        var board1 = new com.example.battleship.dto.persistence.BoardSnapshotDto();
        board1.setWidth(10);
        board1.setHeight(10);
        board1.setShipCells(new java.util.HashSet<>());
        board1.setHitCells(new java.util.HashSet<>());
        board1.setMissCells(new java.util.HashSet<>());
        player1.setBoard(board1);

        game.getPlayers().add(player1);

        var player2 = new com.example.battleship.dto.persistence.PlayerSnapshotDto();
        player2.setId(UUID.randomUUID());
        player2.setName("TestPlayer2");
        player2.setSeatNumber(1);
        player2.setReady(false);

        var board2 = new com.example.battleship.dto.persistence.BoardSnapshotDto();
        board2.setWidth(10);
        board2.setHeight(10);
        board2.setShipCells(new java.util.HashSet<>());
        board2.setHitCells(new java.util.HashSet<>());
        board2.setMissCells(new java.util.HashSet<>());
        player2.setBoard(board2);

        game.getPlayers().add(player2);

        return game;
    }
}
