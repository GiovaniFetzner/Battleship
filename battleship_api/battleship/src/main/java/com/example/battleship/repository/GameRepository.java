package com.example.battleship.repository;

import com.example.battleship.domain.game.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface GameRepository {

    void save(String GameId, Game game);

    Optional<Game> findById(String gameId);

    void deleteById(String gameId);

    Map<String, Game> findAll();

    Optional<Game> findByRoomCode(String roomCode);
}
