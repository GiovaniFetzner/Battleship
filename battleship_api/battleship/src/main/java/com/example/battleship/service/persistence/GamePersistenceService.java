package com.example.battleship.service.persistence;

import com.example.battleship.dto.persistence.GameSnapshotDto;

import java.util.Optional;
import java.util.UUID;

public interface GamePersistenceService {

    GameSnapshotDto saveGame(GameSnapshotDto snapshot);

    Optional<GameSnapshotDto> findGameById(UUID gameId);
}
