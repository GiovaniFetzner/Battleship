package com.example.battleship.repository.jpa;

import com.example.battleship.persistence.entity.GameEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GameJpaRepository extends JpaRepository<GameEntity, UUID> {

    @EntityGraph(attributePaths = { "players", "players.board" })
    @Query("select g from GameEntity g where g.id = :id")
    Optional<GameEntity> findAggregateById(@Param("id") UUID id);

    @Lock(LockModeType.OPTIMISTIC)
    @EntityGraph(attributePaths = { "players", "players.board" })
    @Query("select g from GameEntity g where g.id = :id")
    Optional<GameEntity> findAggregateWithOptimisticLock(@Param("id") UUID id);
}
