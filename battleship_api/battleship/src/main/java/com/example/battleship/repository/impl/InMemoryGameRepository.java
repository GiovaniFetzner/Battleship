package com.example.battleship.repository.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.repository.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, String> roomCodes = new ConcurrentHashMap<>();

    private String generateRoomCode() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 5)
                .toUpperCase();
    }

    @Override
    public Optional<Game> findByRoomCode(String roomCode) {
        String gameId = roomCodes.get(roomCode);
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public void save(String gameId, Game game) {
        games.put(gameId, game);
        String roomCode = generateRoomCode();
        roomCodes.put(roomCode, gameId);
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public void deleteById(String gameId) {
        games.remove(gameId);
        roomCodes.values().removeIf(code -> code.equals(gameId));
    }

    @Override
    public Map<String, Game> findAll() {
        return games;
    }
}
