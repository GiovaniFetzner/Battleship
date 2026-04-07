package com.example.battleship.persistence.adapter;

import com.example.battleship.dto.persistence.GameSnapshotDto;
import com.example.battleship.persistence.entity.GameEntity;
import com.example.battleship.persistence.mapper.GameEntityMapper;
import com.example.battleship.repository.jpa.GameJpaRepository;
import com.example.battleship.service.persistence.port.GameStatePersistencePort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile({ "prod", "test" })
public class JpaGameStatePersistenceAdapter implements GameStatePersistencePort {

    private final GameJpaRepository gameJpaRepository;
    private final GameEntityMapper gameEntityMapper;

    public JpaGameStatePersistenceAdapter(GameJpaRepository gameJpaRepository,
            GameEntityMapper gameEntityMapper) {
        this.gameJpaRepository = gameJpaRepository;
        this.gameEntityMapper = gameEntityMapper;
    }

    @Override
    public GameSnapshotDto save(GameSnapshotDto snapshot) {
        GameEntity gameEntity = resolveEntityForSave(snapshot);
        gameEntityMapper.updateEntityFromSnapshot(snapshot, gameEntity);
        GameEntity saved = gameJpaRepository.save(gameEntity);
        return gameEntityMapper.toSnapshot(saved);
    }

    @Override
    public Optional<GameSnapshotDto> findById(UUID gameId) {
        return gameJpaRepository.findAggregateById(gameId)
                .map(gameEntityMapper::toSnapshot);
    }

    private GameEntity resolveEntityForSave(GameSnapshotDto snapshot) {
        if (snapshot.getId() == null) {
            return GameEntity.createNew();
        }

        // Try to find existing entity for update (with optimistic lock)
        Optional<GameEntity> existing = gameJpaRepository.findAggregateWithOptimisticLock(snapshot.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Entity doesn't exist, create new one with explicit ID
        GameEntity newEntity = GameEntity.createNew();
        newEntity.assignId(snapshot.getId());
        return newEntity;
    }
}
