package com.example.battleship.service.persistence.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.battleship.domain.game.Game;
import com.example.battleship.repository.GameRepository;

@Service
public class AsyncGamePersistenceService {

    private final GameRepository gameRepository;

    public AsyncGamePersistenceService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Async
    public void persist(Game game) {
        gameRepository.save(game);
    }
}