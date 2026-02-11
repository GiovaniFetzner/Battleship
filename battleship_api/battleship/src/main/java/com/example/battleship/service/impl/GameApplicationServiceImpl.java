package com.example.battleship.service.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.game.ShipFactory;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.exception.InvalidMoveException;
import com.example.battleship.mapper.GameMapper;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.service.GameApplicationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameApplicationServiceImpl implements GameApplicationService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;

    public GameApplicationServiceImpl(GameRepository gameRepository,
                                      GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
    }

    @Override
    public GameStateResponse createGame(JoinGameBaseRequest request) {
        String gameId = UUID.randomUUID().toString();

        Player player1 = new Player(request.getPlayerName());
        player1.setShips(ShipFactory.createDefaultShips());

        Game game = new Game(player1, null);
        game.setState(GameState.WAITING);

        gameRepository.save(gameId, game);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, player1.getId());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse joinGame(String gameId, JoinGameBaseRequest request) {
        Game game = getGame(gameId);

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        if (game.getPlayer2() != null) {
            throw new InvalidMoveException("Game is already full!");
        }

        Player player2 = new Player(request.getPlayerName());
        player2.setShips(ShipFactory.createDefaultShips());

        game.setPlayer2(player2);
        game.setState(GameState.WAITING);

        gameRepository.save(gameId, game);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, player2.getId());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse joinGameByCode(String roomCode, String playerName) {
        Game game = gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new InvalidMoveException("Game not found for the provided room code!"));

        if (game.getPlayer2() != null) {
            throw new InvalidMoveException("Game is already full!");
        }

        Player player2 = new Player(playerName);
        player2.setShips(ShipFactory.createDefaultShips());

        game.setPlayer2(player2);
        game.setState(GameState.WAITING);

        gameRepository.save(game.getId(), game);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, player2.getId());
        response.setGameId(game.getId());

        return response;
    }

    @Override
    public GameStateResponse startGame(String gameId) {
        Game game = getGame(gameId);

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        if (game.getPlayer2() == null) {
            throw new InvalidMoveException("Cannot start game without two players!");
        }

        if (game.getState() != GameState.WAITING) {
            throw new InvalidMoveException("Game already started!");
        }

        game.start();
        gameRepository.save(gameId, game);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, game.getCurrentPlayer().getId());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse placeShip(PlaceShipRequest request) {
        Game game = getGame(request.getGameId());
        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        Player player = findPlayer(game, request.getPlayerId());

        if (game.getState() != GameState.WAITING) {
            throw new InvalidMoveException("Cannot place ships after game has started!");
        }

        Ship ship = gameMapper.toShip(request);
        Coordinate coordinate =
                gameMapper.toCoordinate(request.getX(), request.getY());
        Orientation orientation =
                gameMapper.toOrientation(request.getOrientation());

        player.getBoard().placeShip(ship, coordinate, orientation);

        gameRepository.save(game.getId(), game);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, request.getPlayerId());
        response.setGameId(request.getGameId());

        return response;
    }

    @Override
    public AttackResultResponse attack(AttackRequest request) {
        Game game = getGame(request.getGameId());

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        Player player = findPlayer(game, request.getPlayerId());

        if (game.getState() != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is not in progress!");
        }

        if (!game.getCurrentPlayer().equals(player)) {
            throw new InvalidMoveException("It's not your turn!");
        }

        Coordinate coordinate =
                gameMapper.toCoordinate(request.getX(), request.getY());

        AttackResult result = game.attack(coordinate);

        gameRepository.save(game.getId(), game);

        return gameMapper.toAttackResultResponse(result, coordinate, game);
    }

    @Override
    public GameStateResponse getGameState(String gameId, String playerId) {
        Game game = getGame(gameId);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, playerId);
        response.setGameId(gameId);

        return response;
    }

    @Override
    public List<GameStateResponse> listActiveGames() {
        List<GameStateResponse> activeGames = new ArrayList<>();

        gameRepository.findAll().forEach((gameId, game) -> {
            if (game.getState() != GameState.FINISHED) {
                GameStateResponse response =
                        gameMapper.toGameStateResponse(game, "");
                response.setGameId(gameId);
                activeGames.add(response);
            }
        });

        return activeGames;
    }

    @Override
    public void deleteGame(String gameId) {
        gameRepository.deleteById(gameId);
    }

    private Game getGame(String gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }


    private Player findPlayer(Game game, String playerId) {
        if (game.getPlayer1().getId().equals(playerId)) {
            return game.getPlayer1();
        }
        if (game.getPlayer2() != null &&
                game.getPlayer2().getId().equals(playerId)) {
            return game.getPlayer2();
        }
        throw new InvalidMoveException("Player not found in this game!");
    }
}
