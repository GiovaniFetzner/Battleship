package com.example.battleship.repository.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.repository.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public void save(String gameId, Game game) {
        games.put(gameId, game);
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public void deleteById(String gameId) {
        games.remove(gameId);
    }

    @Override
    public Map<String, Game> findAll() {
        return games;
    }
}
