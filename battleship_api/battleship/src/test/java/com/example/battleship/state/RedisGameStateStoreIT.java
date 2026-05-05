package com.example.battleship.state;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.dto.persistence.GameSnapshotDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RedisGameStateStoreIT {

    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0.12"))
            .withExposedPorts(6379);

    @BeforeAll
    static void startContainer() {
        redis.start();
    }

    @AfterAll
    static void stopContainer() {
        redis.stop();
    }

    @Test
    public void saveAndGetGame() {
        String host = redis.getHost();
        Integer port = redis.getFirstMappedPort();

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(host, port);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, GameSnapshotDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(
                new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        RedisGameStateStore store = new RedisGameStateStore(template, new RedisGameSnapshotMapper());

        Game game = new Game(new Player("tester"));
        store.save(game);

        Optional<Game> loaded = store.get(game.getId());

        assertTrue(loaded.isPresent());
        assertEquals(game.getId(), loaded.get().getId());
    }
}
