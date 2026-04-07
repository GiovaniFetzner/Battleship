package com.example.battleship.service.persistence.impl;

import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.service.persistence.GamePersistenceService;
import com.example.battleship.service.persistence.port.GameStatePersistencePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Profile({ "prod", "test" })
public class GamePersistenceServiceImpl implements GamePersistenceService {

    private final GameStatePersistencePort gameStatePersistencePort;

    public GamePersistenceServiceImpl(GameStatePersistencePort gameStatePersistencePort) {
        this.gameStatePersistencePort = gameStatePersistencePort;
    }

    @Override
    @Transactional
    public GameSnapshotDto saveGame(GameSnapshotDto snapshot) {
        return gameStatePersistencePort.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSnapshotDto> findGameById(UUID gameId) {
        return gameStatePersistencePort.findById(gameId);
    }
}
