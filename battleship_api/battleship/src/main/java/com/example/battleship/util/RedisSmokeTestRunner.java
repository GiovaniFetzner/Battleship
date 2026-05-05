package com.example.battleship.util;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.state.GameStateStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("redis-smoke")
public class RedisSmokeTestRunner implements CommandLineRunner {

    private final GameStateStore gameStateStore;

    public RedisSmokeTestRunner(GameStateStore gameStateStore) {
        this.gameStateStore = gameStateStore;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Redis smoke test...");

        Game g = new Game(new Player("smoke-tester"));
        gameStateStore.save(g);
        Game loaded = gameStateStore.get(g.getId()).orElse(null);

        System.out.println("Saved game id: " + g.getId());
        System.out.println("Loaded present: " + (loaded != null));
        System.out.println("Loaded id: " + (loaded != null ? loaded.getId() : "null"));

        // cleanup
        gameStateStore.delete(g.getId());

        // exit VM so Spring Boot doesn't keep running
        System.exit(0);
    }
}
