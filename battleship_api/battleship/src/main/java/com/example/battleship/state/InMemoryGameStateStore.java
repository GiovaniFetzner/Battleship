package com.example.battleship.state;

import com.example.battleship.domain.game.Game;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Primary
@Profile({ "test" })
public class InMemoryGameStateStore implements GameStateStore {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public Optional<Game> get(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public void save(Game game) {
        games.put(game.getId(), game);
    }

    @Override
    public void delete(String gameId) {
        games.remove(gameId);
    }
}