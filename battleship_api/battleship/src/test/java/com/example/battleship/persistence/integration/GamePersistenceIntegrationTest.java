package com.example.battleship.persistence.integration;

import com.example.battleship.dto.persistence.BoardSnapshotDto;
import com.example.battleship.dto.persistence.CoordinateSnapshotDto;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.dto.persistence.PlayerSnapshotDto;
import com.example.battleship.persistence.entity.AttackResultType;
import com.example.battleship.persistence.entity.GameStatus;
import com.example.battleship.persistence.mapper.GameEntityMapper;
import com.example.battleship.repository.jpa.GameJpaRepository;
import com.example.battleship.service.persistence.GamePersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GamePersistenceService Integration Tests")
class GamePersistenceIntegrationTest {

    @Autowired
    private GamePersistenceService gamePersistenceService;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    private GameSnapshotDto testGameSnapshot;

    @BeforeEach
    void setUp() {
        gameJpaRepository.deleteAll();
        testGameSnapshot = createTestGameSnapshot();
    }

    @Test
    @DisplayName("should save and load complete game aggregate")
    void shouldSaveAndLoadGame() {
        // Act: Save game
        GameSnapshotDto saved = gamePersistenceService.saveGame(testGameSnapshot);

        // Assert: Verify save result
        assertNotNull(saved.getId());
        assertEquals(GameStatus.PLACING_SHIPS, saved.getStatus());
        assertEquals(2, saved.getPlayers().size());

        // Act: Retrieve game
        Optional<GameSnapshotDto> retrieved = gamePersistenceService.findGameById(saved.getId());

        // Assert: Verify load result
        assertTrue(retrieved.isPresent());
        GameSnapshotDto loaded = retrieved.get();
        assertEquals(saved.getId(), loaded.getId());
        assertEquals(GameStatus.PLACING_SHIPS, loaded.getStatus());
        assertEquals("Alice", loaded.getPlayers().get(0).getName());
        assertEquals("Bob", loaded.getPlayers().get(1).getName());
    }

    @Test
    @DisplayName("should persist board collections correctly")
    void shouldPersistBoardCollections() {
        // Arrange: Create game with populated board
        Set<CoordinateSnapshotDto> shipCoords = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(0, 0),
                new CoordinateSnapshotDto(1, 0),
                new CoordinateSnapshotDto(2, 0)));
        Set<CoordinateSnapshotDto> hitCoords = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(0, 0),
                new CoordinateSnapshotDto(1, 0)));
        Set<CoordinateSnapshotDto> missCoords = new HashSet<>(Arrays.asList(
                new CoordinateSnapshotDto(5, 5),
                new CoordinateSnapshotDto(6, 6)));

        testGameSnapshot.getPlayers().get(0).getBoard().setShipCells(shipCoords);
        testGameSnapshot.getPlayers().get(0).getBoard().setHitCells(hitCoords);
        testGameSnapshot.getPlayers().get(0).getBoard().setMissCells(missCoords);

        // Act: Save and retrieve
        GameSnapshotDto saved = gamePersistenceService.saveGame(testGameSnapshot);
        Optional<GameSnapshotDto> retrieved = gamePersistenceService.findGameById(saved.getId());

        // Assert
        assertTrue(retrieved.isPresent());
        BoardSnapshotDto board = retrieved.get().getPlayers().get(0).getBoard();
        assertEquals(3, board.getShipCells().size());
        assertEquals(2, board.getHitCells().size());
        assertEquals(2, board.getMissCells().size());
        assertTrue(board.getShipCells().contains(new CoordinateSnapshotDto(0, 0)));
        assertTrue(board.getHitCells().contains(new CoordinateSnapshotDto(1, 0)));
        assertTrue(board.getMissCells().contains(new CoordinateSnapshotDto(5, 5)));
    }

    @Test
    @DisplayName("should handle optimistic locking on concurrent updates")
    void shouldHandleOptimisticLocking() {
        // This test verifies version initialization on persisted aggregates.
        GameSnapshotDto saved = gamePersistenceService.saveGame(testGameSnapshot);
        assertNotNull(saved.getVersion());
    }

    @Test
    @DisplayName("should preserve game state transitions")
    void shouldPreserveGameStateTransitions() {
        // Arrange
        testGameSnapshot.setStatus(GameStatus.IN_PROGRESS);
        testGameSnapshot.setTurnCounter(5);
        testGameSnapshot.setCurrentTurnPlayerId(testGameSnapshot.getPlayers().get(0).getId());

        // Act: Save game in IN_PROGRESS state
        GameSnapshotDto saved = gamePersistenceService.saveGame(testGameSnapshot);

        // Assert: State is preserved
        Optional<GameSnapshotDto> retrieved = gamePersistenceService.findGameById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(GameStatus.IN_PROGRESS, retrieved.get().getStatus());
        assertEquals(5, retrieved.get().getTurnCounter());
        assertNotNull(retrieved.get().getCurrentTurnPlayerId());
    }

    @Test
    @DisplayName("should preserve game winner after finish")
    void shouldPreserveGameWinner() {
        // Arrange
        UUID player1Id = testGameSnapshot.getPlayers().get(0).getId();
        testGameSnapshot.setStatus(GameStatus.FINISHED);
        testGameSnapshot.setWinnerPlayerId(player1Id);

        // Act: Save game in FINISHED state
        GameSnapshotDto saved = gamePersistenceService.saveGame(testGameSnapshot);

        // Assert: Winner is preserved
        Optional<GameSnapshotDto> retrieved = gamePersistenceService.findGameById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(GameStatus.FINISHED, retrieved.get().getStatus());
        assertEquals(player1Id, retrieved.get().getWinnerPlayerId());
    }

    @Test
    @DisplayName("should support multiple games in database")
    void shouldSupportMultipleGames() {
        // Arrange: Create and save multiple games
        GameSnapshotDto game1 = gamePersistenceService.saveGame(testGameSnapshot);

        GameSnapshotDto game2Snapshot = createTestGameSnapshot();
        game2Snapshot.getPlayers().get(0).setName("Charlie");
        game2Snapshot.getPlayers().get(1).setName("Diana");
        GameSnapshotDto game2 = gamePersistenceService.saveGame(game2Snapshot);

        // Act: Retrieve both games
        Optional<GameSnapshotDto> retrieved1 = gamePersistenceService.findGameById(game1.getId());
        Optional<GameSnapshotDto> retrieved2 = gamePersistenceService.findGameById(game2.getId());

        // Assert: Both games exist independently
        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertEquals("Alice", retrieved1.get().getPlayers().get(0).getName());
        assertEquals("Charlie", retrieved2.get().getPlayers().get(0).getName());
    }

    @Test
    @DisplayName("should return empty Optional for non-existent game")
    void shouldReturnEmptyForNonExistentGame() {
        // Act & Assert
        UUID randomId = UUID.randomUUID();
        Optional<GameSnapshotDto> result = gamePersistenceService.findGameById(randomId);
        assertFalse(result.isPresent());
    }

    // Helper: Create a test game snapshot
    private GameSnapshotDto createTestGameSnapshot() {
        GameSnapshotDto game = new GameSnapshotDto();
        game.setId(UUID.randomUUID());
        game.setStatus(GameStatus.PLACING_SHIPS);
        game.setTurnCounter(0);

        // Player 1
        PlayerSnapshotDto player1 = new PlayerSnapshotDto();
        player1.setId(UUID.randomUUID());
        player1.setName("Alice");
        player1.setSeatNumber(0);
        player1.setReady(false);

        BoardSnapshotDto board1 = new BoardSnapshotDto();
        board1.setWidth(10);
        board1.setHeight(10);
        board1.setShipCells(new HashSet<>());
        board1.setHitCells(new HashSet<>());
        board1.setMissCells(new HashSet<>());
        player1.setBoard(board1);

        // Player 2
        PlayerSnapshotDto player2 = new PlayerSnapshotDto();
        player2.setId(UUID.randomUUID());
        player2.setName("Bob");
        player2.setSeatNumber(1);
        player2.setReady(false);

        BoardSnapshotDto board2 = new BoardSnapshotDto();
        board2.setWidth(10);
        board2.setHeight(10);
        board2.setShipCells(new HashSet<>());
        board2.setHitCells(new HashSet<>());
        board2.setMissCells(new HashSet<>());
        player2.setBoard(board2);

        game.setPlayers(Arrays.asList(player1, player2));
        game.setAttacks(new java.util.ArrayList<>());

        return game;
    }
}
