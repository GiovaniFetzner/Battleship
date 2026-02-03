package com.example.battleship.config;

import com.example.battleship.repository.GameRepository;
import com.example.battleship.repository.impl.InMemoryGameRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public GameRepository gameRepository() {
        return new InMemoryGameRepository();
    }
}
