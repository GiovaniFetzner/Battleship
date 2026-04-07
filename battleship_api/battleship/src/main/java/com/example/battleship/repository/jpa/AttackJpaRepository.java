package com.example.battleship.repository.jpa;

import com.example.battleship.persistence.entity.AttackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttackJpaRepository extends JpaRepository<AttackEntity, UUID> {
}
