package com.example.battleship.service.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.service.GameService;
import com.example.battleship.service.persistence.async.AsyncGamePersistenceService;
import com.example.battleship.state.GameStateStore;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    // Camada de persistência utilizando o Redis
    private final GameStateStore gameStateStore;
    private static final int SAVE_INTERVAL = 5;
    private final Map<String, Long> lastSave = new ConcurrentHashMap<>();

    private final AsyncGamePersistenceService asyncGamePersistenceService;

    public GameServiceImpl(GameRepository gameRepository, GameStateStore gameStateStore,
            AsyncGamePersistenceService asyncGamePersistenceService) {
        this.gameRepository = gameRepository;
        this.gameStateStore = gameStateStore;
        this.asyncGamePersistenceService = asyncGamePersistenceService;
    }

    private void saveGame(Game game) {
        gameStateStore.save(game);
        persistIfNeeded(game);
    }

    private void persistIfNeeded(Game game) {
        long now = System.currentTimeMillis();
        long lastSaved = lastSave.getOrDefault(game.getId(), 0L);

        if (game.getState() == GameState.FINISHED) {
            asyncGamePersistenceService.persist(game);
            lastSave.put(game.getId(), now);
            return;
        }

        if (now - lastSaved < 3000) {
            return;
        }

        if (game.getTurnCounter() % SAVE_INTERVAL == 0) {
            asyncGamePersistenceService.persist(game);
            lastSave.put(game.getId(), now);
        }
    }

    @Override
    public Game createGame(String playerName) {

        Player player1 = new Player(playerName);

        Game game = new Game(player1);

        saveGame(game);

        return game;
    }

    @Override
    public Game joinGame(String gameId, String playerName) {

        Game game = getGameOrThrow(gameId);

        Player player2 = new Player(playerName);

        game.addPlayer2(player2);

        saveGame(game);

        return game;
    }

    @Override
    public Game placeShip(String gameId,
            String playerName,
            String shipType,
            int size,
            int x,
            int y,
            String orientation) {

        Game game = getGameOrThrow(gameId);

        Coordinate coordinate = new Coordinate(x, y);
        Orientation orient = Orientation.valueOf(orientation);
        Ship ship = new Ship(shipType, size);

        game.placeShip(playerName, ship, coordinate, orient);

        saveGame(game);

        return game;
    }

    @Override
    public AttackResult attack(String gameId,
            String playerName,
            int x,
            int y) {

        Game game = getGameOrThrow(gameId);

        AttackResult result = game.attack(playerName, new Coordinate(x, y));

        saveGame(game);

        return result;
    }

    @Override
    public boolean confirmPlayerReady(String gameId, String playerName) {

        Game game = getGameOrThrow(gameId);

        game.confirmPlacement(playerName);

        saveGame(game);

        return game.getState() == GameState.IN_PROGRESS;
    }

    @Override
    public Game getGameState(String gameId) {
        return getGameOrThrow(gameId);
    }

    @Override
    public List<Game> listActiveGames() {
        return gameRepository.findAll()
                .values()
                .stream()
                .filter(game -> game.getState() != GameState.FINISHED)
                .toList();
    }

    @Override
    public List<Game> listAvailableGames() {
        return gameRepository.findAll()
                .values()
                .stream()
                .filter(game -> game.getState() == GameState.WAITING_FOR_PLAYERS)
                .toList();
    }

    @Override
    public void deleteGame(String gameId) {

        getGameOrThrow(gameId);

        gameStateStore.delete(gameId);
        lastSave.remove(gameId);
        gameRepository.deleteById(gameId);
    }

    private Game getGameOrThrow(String gameId) {
        return gameStateStore.get(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    @Override
    public String getCurrentPlayer(String gameId) {

        Game game = getGameOrThrow(gameId);

        return game.getCurrentPlayer().getName();
    }

    @Override
    public boolean isGameOver(String gameId) {

        Game game = getGameOrThrow(gameId);

        return game.getState() == GameState.FINISHED;
    }

    @Override
    public String getWinner(String gameId) {

        Game game = getGameOrThrow(gameId);

        Player winner = game.getWinner();

        return winner != null ? winner.getName() : null;
    }

}
