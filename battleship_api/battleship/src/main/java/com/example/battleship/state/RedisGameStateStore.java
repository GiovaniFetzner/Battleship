package com.example.battleship.state;

import com.example.battleship.domain.game.Game;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile({ "prod", "redis-smoke" })
public class RedisGameStateStore implements GameStateStore {

    private final RedisTemplate<String, GameSnapshotDto> redisTemplate;
    private final RedisGameSnapshotMapper snapshotMapper;

    public RedisGameStateStore(RedisTemplate<String, GameSnapshotDto> redisTemplate,
            RedisGameSnapshotMapper snapshotMapper) {
        this.redisTemplate = redisTemplate;
        this.snapshotMapper = snapshotMapper;
    }

    @Override
    public Optional<Game> get(String gameId) {
        GameSnapshotDto snapshot = redisTemplate.opsForValue().get(key(gameId));
        return Optional.ofNullable(snapshot).map(snapshotMapper::toDomain);
    }

    @Override
    public void save(Game game) {
        redisTemplate.opsForValue().set(key(game.getId()), snapshotMapper.toSnapshot(game), Duration.ofHours(1));
    }

    @Override
    public void delete(String gameId) {
        redisTemplate.delete(key(gameId));
    }

    private String key(String gameId) {
        return "game:" + gameId;
    }
}