package com.example.battleship.service;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.exception.InvalidMoveException;
import com.example.battleship.mapper.GameMapper;
import com.example.battleship.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private GameMapper gameMapper;

    @BeforeEach
    void setUp() {
        gameMapper = new GameMapper();
        gameService = new GameServiceImpl(gameMapper);
    }

    @Test
    void shouldCreateGameSuccessfully() {
        JoinGameRequest request = new JoinGameRequest("Player1");

        GameStateResponse response = gameService.createGame(request);

        assertNotNull(response);
        assertNotNull(response.getGameId());
        assertEquals("WAITING", response.getState());
        assertEquals("Player1", response.getPlayer1());
        assertNull(response.getPlayer2());
    }

    @Test
    void shouldJoinGameSuccessfully() {
        JoinGameRequest player1Request = new JoinGameRequest("Player1");
        GameStateResponse createResponse = gameService.createGame(player1Request);
        String gameId = createResponse.getGameId();

        JoinGameRequest player2Request = new JoinGameRequest("Player2");

        GameStateResponse response = gameService.joinGame(gameId, player2Request);

        assertNotNull(response);
        assertEquals(gameId, response.getGameId());
        assertEquals("WAITING", response.getState());
        assertEquals("Player1", response.getPlayer1());
        assertEquals("Player2", response.getPlayer2());
    }

    @Test
    void shouldNotJoinFullGame() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));
        gameService.joinGame(game.getGameId(), new JoinGameRequest("Player2"));

        assertThrows(InvalidMoveException.class, () -> {
            gameService.joinGame(game.getGameId(), new JoinGameRequest("Player3"));
        });
    }

    @Test
    void shouldStartGameSuccessfully() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));
        gameService.joinGame(game.getGameId(), new JoinGameRequest("Player2"));

        GameStateResponse response = gameService.startGame(game.getGameId());

        assertEquals("IN_PROGRESS", response.getState());
        assertNotNull(response.getCurrentPlayer());
    }

    @Test
    void shouldNotStartGameWithoutTwoPlayers() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));

        assertThrows(InvalidMoveException.class, () -> {
            gameService.startGame(game.getGameId());
        });
    }

    @Test
    void shouldPlaceShipSuccessfully() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        GameStateResponse response = gameService.placeShip(request);

        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
    }

    @Test
    void shouldNotPlaceShipAfterGameStarts() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));
        gameService.joinGame(game.getGameId(), new JoinGameRequest("Player2"));
        gameService.startGame(game.getGameId());

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        assertThrows(InvalidMoveException.class, () -> {
            gameService.placeShip(request);
        });
    }

    @Test
    void shouldAttackSuccessfully() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));
        gameService.joinGame(game.getGameId(), new JoinGameRequest("Player2"));

        gameService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player1", "Ship", 2, 0, 0, "HORIZONTAL"
        ));
        gameService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player2", "Ship", 2, 5, 5, "HORIZONTAL"
        ));

        gameService.startGame(game.getGameId());

        AttackRequest request = new AttackRequest(game.getGameId(), "Player1", 5, 5);

        AttackResultResponse response = gameService.attack(request);

        assertNotNull(response);
        assertEquals("HIT", response.getResult());
        assertEquals(5, response.getX());
        assertEquals(5, response.getY());
    }

    @Test
    void shouldNotAttackWhenNotYourTurn() {
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));
        gameService.joinGame(game.getGameId(), new JoinGameRequest("Player2"));
        gameService.startGame(game.getGameId());

        // Player2 tenta atacar mas é turno do Player1
        AttackRequest request = new AttackRequest(game.getGameId(), "Player2", 0, 0);

         & Then
        assertThrows(InvalidMoveException.class, () -> {
            gameService.attack(request);
        });
    }

    @Test
    void shouldGetGameState() {
        
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));

        
        GameStateResponse response = gameService.getGameState(game.getGameId(), "Player1");

        
        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
        assertEquals("WAITING", response.getState());
    }

    @Test
    void shouldListActiveGames() {
        
        gameService.createGame(new JoinGameRequest("Player1"));
        gameService.createGame(new JoinGameRequest("Player2"));

        
        var games = gameService.listActiveGames();

        
        assertEquals(2, games.size());
    }

    @Test
    void shouldDeleteGame() {
        
        GameStateResponse game = gameService.createGame(new JoinGameRequest("Player1"));

        
        gameService.deleteGame(game.getGameId());

        
        assertThrows(InvalidMoveException.class, () -> {
            gameService.getGameState(game.getGameId(), "Player1");
        });
    }
}
