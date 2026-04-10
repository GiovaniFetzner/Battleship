package com.example.battleship.service;

import com.example.battleship.domain.game.Game;
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
        game = new Game(p1);
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

        assertNotNull(result);
        assertEquals("player2", result.findPlayer("player2").getName());
        verify(gameRepository).save(game);
    }

    @Test
    void shouldThrowWhenGameFull() {

        game.addPlayer2(new Player("existing"));

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        assertThrows(InvalidMoveException.class, () -> gameService.joinGame("1", "player2"));
    }

    // =========================
    // placeShip
    // =========================

    @Test
    void shouldPlaceShip() {

        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        // require two players to be in PLACING_SHIPS phase
        game.addPlayer2(new Player("player2"));

        Game result = gameService.placeShip("1", "player1", "Destroyer", 2, 0, 0, "HORIZONTAL");

        assertNotNull(result);
        verify(gameRepository).save(game);
    }

    // =========================
    // attack
    // =========================

    @Test
    void shouldAttack() {

        // Prepare game with both players and place the required fleet (5+4+3+2 = 14
        // cells)
        game.addPlayer2(new Player("player2"));
        when(gameRepository.findById("1")).thenReturn(Optional.of(game));

        gameService.placeShip("1", "player1", "Carrier", 5, 0, 0, "HORIZONTAL");
        gameService.placeShip("1", "player1", "Battleship", 4, 0, 1, "HORIZONTAL");
        gameService.placeShip("1", "player1", "Submarine", 3, 0, 2, "HORIZONTAL");
        gameService.placeShip("1", "player1", "Patrol", 2, 0, 3, "HORIZONTAL");

        gameService.placeShip("1", "player2", "Carrier", 5, 0, 5, "HORIZONTAL");
        gameService.placeShip("1", "player2", "Battleship", 4, 0, 6, "HORIZONTAL");
        gameService.placeShip("1", "player2", "Submarine", 3, 0, 7, "HORIZONTAL");
        gameService.placeShip("1", "player2", "Patrol", 2, 0, 8, "HORIZONTAL");

        // Confirm placement through service (moves game to IN_PROGRESS)
        gameService.confirmPlayerReady("1", "player1");
        gameService.confirmPlayerReady("1", "player2");

        AttackResult result = gameService.attack("1", "player1", 0, 0);

        assertNotNull(result);
        verify(gameRepository, atLeastOnce()).save(game);
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
