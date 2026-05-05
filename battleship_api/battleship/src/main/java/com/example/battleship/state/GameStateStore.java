package com.example.battleship.state;

import com.example.battleship.domain.game.Game;

import java.util.Optional;

public interface GameStateStore {
    Optional<Game> get(String gameId);

    void save(Game game);

    void delete(String gameId);
}