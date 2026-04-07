package com.example.battleship.service.persistence.port;

import com.example.battleship.dto.persistence.GameSnapshotDto;

import java.util.Optional;
import java.util.UUID;

public interface GameStatePersistencePort {

    GameSnapshotDto save(GameSnapshotDto snapshot);

    Optional<GameSnapshotDto> findById(UUID gameId);
}
