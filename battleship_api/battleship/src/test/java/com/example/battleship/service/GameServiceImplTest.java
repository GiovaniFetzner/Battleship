package com.example.battleship.service;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.exception.InvalidMoveException;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game;

    @BeforeEach
    void setup() {
        Player p1 = new Player("player1");
        Player p2 = new Player("player2");

        game = new Game(p1, null);
    }

    // =========================
    // createGame
    // =========================

    @Test
    void shouldCreateGame() {

        Game result = gameService.createGame("player1");

        assertNotNull(result);
        assertEquals("player1", result.getPlayer1().getName());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    // =========================
    // joinGame
    // =========================

    @Test
    void shouldJoinGame() {

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        Game result = gameService.joinGame("1", "player2");

        assertNotNull(result.getPlayer2());
        assertEquals("player2", result.getPlayer2().getName());
        verify(gameRepository).save(game);
    }

    @Test
    void shouldThrowWhenGameFull() {

        game.setPlayer2(new Player("existing"));

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        assertThrows(InvalidMoveException.class,
                () -> gameService.joinGame("1", "player2"));
    }

    // =========================
    // startGame
    // =========================

    @Test
    void shouldStartGame() {

        game.setPlayer2(new Player("player2"));

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        Game result = gameService.startGame("1");

        assertEquals(GameState.IN_PROGRESS, result.getState());
        verify(gameRepository).save(game);
    }

    // =========================
    // placeShip
    // =========================

    @Test
    void shouldPlaceShip() {

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        Game result = gameService.placeShip(
                "1",
                "player1",
                "Destroyer",
                2,
                0,
                0,
                "HORIZONTAL"
        );

        assertNotNull(result);
        verify(gameRepository).save(game);
    }

    // =========================
    // attack
    // =========================

    @Test
    void shouldAttack() {

        Player p2 = new Player("player2");
        game.setPlayer2(p2);
        game.start();

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        AttackResult result =
                gameService.attack("1", "player1", 0, 0);

        assertNotNull(result);
        verify(gameRepository).save(game);
    }

    // =========================
    // getGameState
    // =========================

    @Test
    void shouldReturnGameState() {

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        Game result = gameService.getGameState("1");

        assertEquals(game, result);
    }

    // =========================
    // listActiveGames
    // =========================

    @Test
    void shouldListActiveGames() {

        when(gameRepository.findAll())
                .thenReturn(Map.of("1", game));

        var games = gameService.listActiveGames();

        assertEquals(1, games.size());
    }

    // =========================
    // deleteGame
    // =========================

    @Test
    void shouldDeleteGame() {

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        gameService.deleteGame("1");

        verify(gameRepository).deleteById("1");
    }
}
