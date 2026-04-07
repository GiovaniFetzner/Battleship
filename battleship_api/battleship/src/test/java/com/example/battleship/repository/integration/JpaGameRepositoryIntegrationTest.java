package com.example.battleship.repository.integration;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.repository.jpa.GameJpaRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JpaGameRepository Integration Tests")
class JpaGameRepositoryIntegrationTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameJpaRepository springDataGameJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        springDataGameJpaRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("should save and load game")
    void shouldSaveAndLoadGame() {
        Game game = new Game(new Player("Alice"));
        game.addPlayer2(new Player("Bob"));

        gameRepository.save(game);

        Optional<Game> loaded = gameRepository.findById(game.getId());

        assertTrue(loaded.isPresent());
        assertEquals(game.getId(), loaded.get().getId());
        assertEquals("Alice", loaded.get().getPlayer1().getName());
        assertEquals("Bob", loaded.get().getPlayer2().getName());
    }

    @Test
    @Transactional
    @DisplayName("should persist board collections")
    void shouldPersistBoardCollections() {
        Game game = new Game(new Player("Alice"));
        game.addPlayer2(new Player("Bob"));

        game.getPlayer1().getBoard().placeShip(new Ship("Destroyer", 2), new Coordinate(0, 0), Orientation.HORIZONTAL);
        game.getPlayer1().getBoard().attack(new Coordinate(0, 0));
        game.getPlayer1().getBoard().attack(new Coordinate(4, 4));

        gameRepository.save(game);

        GameEntity persisted = springDataGameJpaRepository.findAll().stream().findFirst().orElseThrow();
        var board = persisted.getPlayers().stream()
                .findFirst()
                .orElseThrow()
                .getBoard();

        assertEquals(2, board.getShipCells().size());
        assertEquals(1, board.getHitCells().size());
        assertEquals(1, board.getMissCells().size());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("should handle optimistic locking")
    void shouldHandleOptimisticLocking() throws Exception {
        Game game = new Game(new Player("Alice"));
        game.addPlayer2(new Player("Bob"));
        gameRepository.save(game);

        UUID gameId = UUID.fromString(game.getId());
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        GameEntity staleCopyA = txTemplate
                .execute(status -> springDataGameJpaRepository.findById(gameId).orElseThrow());
        GameEntity staleCopyB = txTemplate
                .execute(status -> springDataGameJpaRepository.findById(gameId).orElseThrow());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> first = executor.submit(() -> runUpdateWithDetachedEntity(staleCopyA, txTemplate));
            Future<Void> second = executor.submit(() -> runUpdateWithDetachedEntity(staleCopyB, txTemplate));

            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

            boolean optimisticLockDetected = false;
            optimisticLockDetected |= isOptimisticLockFailure(first);
            optimisticLockDetected |= isOptimisticLockFailure(second);

            assertTrue(optimisticLockDetected, "Expected an OptimisticLockException in one of the concurrent updates");
        } finally {
            executor.shutdownNow();
        }
    }

    private Void runUpdateWithDetachedEntity(GameEntity staleEntity,
            TransactionTemplate txTemplate) {
        return txTemplate.execute(status -> {
            staleEntity.setTurnCounter(staleEntity.getTurnCounter() + 1);
            springDataGameJpaRepository.saveAndFlush(staleEntity);
            return null;
        });
    }

    private boolean isOptimisticLockFailure(Future<Void> future) throws Exception {
        try {
            future.get();
            return false;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            return containsOptimisticLockException(cause);
        }
    }

    private boolean containsOptimisticLockException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof OptimisticLockException
                    || current instanceof ObjectOptimisticLockingFailureException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
