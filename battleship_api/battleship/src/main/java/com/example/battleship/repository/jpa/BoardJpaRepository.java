package com.example.battleship.repository.jpa;

import com.example.battleship.persistence.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardJpaRepository extends JpaRepository<BoardEntity, UUID> {
}
