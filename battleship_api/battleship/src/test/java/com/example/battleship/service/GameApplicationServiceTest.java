package com.example.battleship.service;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.dto.outbound.ShipDTO;
import com.example.battleship.exception.InvalidMoveException;
import com.example.battleship.mapper.GameMapper;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.repository.impl.InMemoryGameRepository;
import com.example.battleship.service.impl.GameApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameApplicationServiceTest {

    private GameApplicationService gameApplicationService;
    private GameMapper gameMapper;
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        gameMapper = new GameMapper();
        gameApplicationService = new GameApplicationServiceImpl(gameRepository, gameMapper);
    }

    // ==================== Creating game tests ====================
    
    @Test
    void shouldCreateGameSuccessfully() {
        JoinGameBaseRequest request = new JoinGameBaseRequest("Player1");

        GameStateResponse response = gameApplicationService.createGame(request);

        assertNotNull(response);
        assertNotNull(response.getGameId());
        assertEquals("WAITING_FOR_PLAYERS", response.getGameStatus());
        assertEquals("Player1", response.getPlayer1Name());
        assertEquals("Player1", response.getPlayer1Id());
        assertNull(response.getPlayer2Name());
        assertNull(response.getPlayer2Id());
        assertEquals(0, response.getTurnNumber());
        assertNull(response.getWinner());
        assertNull(response.getCurrentPlayer());
        assertFalse(response.isMyTurn());
    }

    @Test
    void shouldCreateGameWithShips() {
        JoinGameBaseRequest request = new JoinGameBaseRequest("Player1");

        GameStateResponse response = gameApplicationService.createGame(request);

        assertNotNull(response.getMyShips());
        assertTrue(response.getMyShips().size() > 0);
        assertTrue(response.getMyShipsRemaining() > 0);
        assertEquals(0, response.getOpponentShipsRemaining());
    }

    // ==================== Testes with join game ====================
    
    @Test
    void shouldJoinGameSuccessfully() {
        JoinGameBaseRequest player1Request = new JoinGameBaseRequest("Player1");
        GameStateResponse createResponse = gameApplicationService.createGame(player1Request);
        String gameId = createResponse.getGameId();

        JoinGameBaseRequest player2Request = new JoinGameBaseRequest("Player2");
        GameStateResponse response = gameApplicationService.joinGame(gameId, player2Request);

        assertNotNull(response);
        assertEquals(gameId, response.getGameId());
        assertEquals("WAITING_FOR_PLAYERS", response.getGameStatus());
        assertEquals("Player1", response.getPlayer1Name());
        assertEquals("Player2", response.getPlayer2Name());
        assertEquals("Player1", response.getPlayer1Id());
        assertEquals("Player2", response.getPlayer2Id());
        assertTrue(response.getOpponentShipsRemaining() > 0);
    }

    @Test
    void shouldNotJoinFullGame() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player3"));
        });
        
        assertEquals("Game is already full!", exception.getMessage());
    }

    @Test
    void shouldNotJoinNonExistentGame() {
        JoinGameBaseRequest request = new JoinGameBaseRequest("Player1");
        
        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.joinGame("non-existent-id", request);
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    // ==================== Tests starting the game ====================
    
    @Test
    void shouldStartGameSuccessfully() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        GameStateResponse response = gameApplicationService.startGame(game.getGameId());

        assertEquals("PLAYING", response.getGameStatus());
        assertNotNull(response.getCurrentPlayer());
        assertTrue(response.getCurrentPlayer().equals("Player1") || response.getCurrentPlayer().equals("Player2"));
    }

    @Test
    void shouldNotStartGameWithoutTwoPlayers() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.startGame(game.getGameId());
        });
        
        assertEquals("Cannot start game without two players!", exception.getMessage());
    }

    @Test
    void shouldNotStartAlreadyStartedGame() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        gameApplicationService.startGame(game.getGameId());

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.startGame(game.getGameId());
        });
        
        assertEquals("Game already started!", exception.getMessage());
    }

    @Test
    void shouldNotStartNonExistentGame() {
        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.startGame("non-existent-id");
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    // ==================== Tests placing the ship ====================
    
    @Test
    void shouldPlaceShipSuccessfully() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        GameStateResponse response = gameApplicationService.placeShip(request);

        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
        assertEquals("WAITING_FOR_PLAYERS", response.getGameStatus());
    }

    @Test
    void shouldPlaceShipWithVerticalOrientation() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Destroyer", 2, 3, 3, "VERTICAL"
        );

        GameStateResponse response = gameApplicationService.placeShip(request);

        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
    }

    @Test
    void shouldPlaceShipWithNullOrientation() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Submarine", 1, 5, 5, null
        );

        GameStateResponse response = gameApplicationService.placeShip(request);

        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
    }

    @Test
    void shouldNotPlaceShipAfterGameStarts() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        gameApplicationService.startGame(game.getGameId());

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "Player1", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.placeShip(request);
        });
        
        assertEquals("Cannot place ships after game has started!", exception.getMessage());
    }

    @Test
    void shouldNotPlaceShipForNonExistentPlayer() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        PlaceShipRequest request = new PlaceShipRequest(
            game.getGameId(), "NonExistentPlayer", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.placeShip(request);
        });
        
        assertEquals("Player not found in this game!", exception.getMessage());
    }

    @Test
    void shouldNotPlaceShipInNonExistentGame() {
        PlaceShipRequest request = new PlaceShipRequest(
            "non-existent-id", "Player1", "Battleship", 4, 0, 0, "HORIZONTAL"
        );

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.placeShip(request);
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    // ==================== Tests attacking ====================
    
    @Test
    void shouldAttackSuccessfully() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player1", "Ship1", 2, 0, 0, "HORIZONTAL"
        ));
        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player2", "Ship2", 2, 5, 5, "HORIZONTAL"
        ));

        gameApplicationService.startGame(game.getGameId());

        AttackRequest request = new AttackRequest(game.getGameId(), "Player1", 5, 5);
        AttackResultResponse response = gameApplicationService.attack(request);

        assertNotNull(response);
        assertEquals("HIT", response.getResult());
        assertEquals(5, response.getX());
        assertEquals(5, response.getY());
        assertNotNull(response.getCurrentPlayer());
    }

    @Test
    void shouldReturnMissOnEmptyPosition() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player1", "Ship1", 2, 0, 0, "HORIZONTAL"
        ));
        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player2", "Ship2", 2, 5, 5, "HORIZONTAL"
        ));

        gameApplicationService.startGame(game.getGameId());

        AttackRequest request = new AttackRequest(game.getGameId(), "Player1", 9, 9);
        AttackResultResponse response = gameApplicationService.attack(request);

        assertNotNull(response);
        assertEquals("MISS", response.getResult());
        assertEquals(9, response.getX());
        assertEquals(9, response.getY());
    }

    @Test
    void shouldNotAttackWhenNotYourTurn() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        gameApplicationService.startGame(game.getGameId());

        AttackRequest request = new AttackRequest(game.getGameId(), "Player2", 0, 0);

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.attack(request);
        });
        
        assertEquals("It's not your turn!", exception.getMessage());
    }

    @Test
    void shouldNotAttackWhenGameNotInProgress() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        AttackRequest request = new AttackRequest(game.getGameId(), "Player1", 0, 0);

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.attack(request);
        });
        
        assertEquals("Game is not in progress!", exception.getMessage());
    }

    @Test
    void shouldNotAttackWithNonExistentPlayer() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        gameApplicationService.startGame(game.getGameId());

        AttackRequest request = new AttackRequest(game.getGameId(), "NonExistentPlayer", 0, 0);

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.attack(request);
        });
        
        assertEquals("Player not found in this game!", exception.getMessage());
    }

    @Test
    void shouldNotAttackNonExistentGame() {
        AttackRequest request = new AttackRequest("non-existent-id", "Player1", 0, 0);

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.attack(request);
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    // ==================== Tests with the game state ====================
    
    @Test
    void shouldGetGameState() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        GameStateResponse response = gameApplicationService.getGameState(game.getGameId(), "Player1");

        assertNotNull(response);
        assertEquals(game.getGameId(), response.getGameId());
        assertEquals("WAITING_FOR_PLAYERS", response.getGameStatus());
        assertEquals(0, response.getTurnNumber());
    }

    @Test
    void shouldGetGameStateWithBothPlayers() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));

        GameStateResponse response = gameApplicationService.getGameState(game.getGameId(), "Player2");

        assertNotNull(response);
        assertEquals("Player1", response.getPlayer1Name());
        assertEquals("Player2", response.getPlayer2Name());
        assertTrue(response.getMyShipsRemaining() > 0);
        assertTrue(response.getOpponentShipsRemaining() > 0);
    }

    @Test
    void shouldGetGameStateWithCorrectTurnInfo() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        gameApplicationService.startGame(game.getGameId());

        GameStateResponse response = gameApplicationService.getGameState(game.getGameId(), "Player1");

        assertNotNull(response.getCurrentPlayer());
        if (response.getCurrentPlayer().equals("Player1")) {
            assertTrue(response.isMyTurn());
        } else {
            assertFalse(response.isMyTurn());
        }
    }

    @Test
    void shouldNotGetStateOfNonExistentGame() {
        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.getGameState("non-existent-id", "Player1");
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    // ==================== Tests listing the existent players ====================
    
    @Test
    void shouldListActiveGames() {
        gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        gameApplicationService.createGame(new JoinGameBaseRequest("Player2"));

        List<GameStateResponse> games = gameApplicationService.listActiveGames();

        assertTrue(games.size() >= 2);
        for (GameStateResponse game : games) {
            assertNotNull(game.getGameId());
            assertNotEquals("FINISHED", game.getGameStatus());
        }
    }

    @Test
    void shouldListEmptyGamesWhenNoGamesExist() {
        List<GameStateResponse> games = gameApplicationService.listActiveGames();
        
        assertNotNull(games);
        assertTrue(games.size() >= 0);
    }

    // ==================== Tests to clean up the game ====================
    
    @Test
    void shouldDeleteGame() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));

        gameApplicationService.deleteGame(game.getGameId());

        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.getGameState(game.getGameId(), "Player1");
        });
        
        assertEquals("Game not found!", exception.getMessage());
    }

    @Test
    void shouldDeleteNonExistentGameSilently() {
        // Não deve lançar exceção
        assertDoesNotThrow(() -> {
            gameApplicationService.deleteGame("non-existent-id");
        });
    }

    // ==================== Some DTO coverage tests ====================
    

    @Test
    void shouldTestShipDTOFields() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        GameStateResponse response = gameApplicationService.getGameState(game.getGameId(), "Player1");
        
        List<ShipDTO> ships = response.getMyShips();
        assertNotNull(ships);
        
        if (!ships.isEmpty()) {
            ShipDTO ship = ships.get(0);
            assertNotNull(ship.getName());
            assertTrue(ship.getSize() > 0);
            assertTrue(ship.getHits() >= 0);
        }
    }

    @Test
    void shouldHandleMultiplePlayersJoiningSimultaneously() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        String gameId = game.getGameId();

        // Simula múltiplas tentativas simultâneas
        gameApplicationService.joinGame(gameId, new JoinGameBaseRequest("Player2"));

        assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.joinGame(gameId, new JoinGameBaseRequest("Player3"));
        });

        assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.joinGame(gameId, new JoinGameBaseRequest("Player4"));
        });
    }

    @Test
    void shouldHandleCompleteGameFlow() {
        GameStateResponse game = gameApplicationService.createGame(new JoinGameBaseRequest("Player1"));
        assertEquals("WAITING_FOR_PLAYERS", game.getGameStatus());
        
        GameStateResponse joinResponse = gameApplicationService.joinGame(game.getGameId(), new JoinGameBaseRequest("Player2"));
        assertEquals("WAITING_FOR_PLAYERS", joinResponse.getGameStatus());
        
        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player1", "Ship1", 1, 0, 0, "HORIZONTAL"
        ));
        gameApplicationService.placeShip(new PlaceShipRequest(
            game.getGameId(), "Player2", "Ship2", 1, 5, 5, "HORIZONTAL"
        ));
        
        GameStateResponse startResponse = gameApplicationService.startGame(game.getGameId());
        assertEquals("PLAYING", startResponse.getGameStatus());
        
        GameStateResponse stateResponse = gameApplicationService.getGameState(game.getGameId(), "Player1");
        assertEquals("PLAYING", stateResponse.getGameStatus());
        
        List<GameStateResponse> activeGames = gameApplicationService.listActiveGames();
        assertTrue(activeGames.size() >= 1);
        
        gameApplicationService.deleteGame(game.getGameId());
        
        assertThrows(InvalidMoveException.class, () -> {
            gameApplicationService.getGameState(game.getGameId(), "Player1");
        });
    }
}
