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
import com.example.battleship.service.GameService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final GameMapper gameMapper;

    public GameServiceImpl(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    @Override
    public GameStateResponse createGame(JoinGameBaseRequest request) {
        String gameId = UUID.randomUUID().toString();

        Player player1 = new Player(request.getPlayerName());
        player1.setShips(ShipFactory.createDefaultShips());

        Game game = new Game(player1, null);
        game.setState(GameState.WAITING);
        games.put(gameId, game);

        GameStateResponse response = gameMapper.toGameStateResponse(game, request.getPlayerName());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse joinGame(String gameId, JoinGameBaseRequest request) {
        Game game = games.get(gameId);
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

        GameStateResponse response = gameMapper.toGameStateResponse(game, request.getPlayerName());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse startGame(String gameId) {
        Game game = getGame(gameId);

        if (game.getPlayer2() == null) {
            throw new InvalidMoveException("Cannot start game without two players!");
        }

        if (game.getState() != GameState.WAITING) {
            throw new InvalidMoveException("Game already started!");
        }

        game.start();

        GameStateResponse response = gameMapper.toGameStateResponse(game, game.getPlayer1().getId());
        response.setGameId(gameId);

        return response;
    }

    @Override
    public GameStateResponse placeShip(PlaceShipRequest request) {
        Game game = getGame(request.getGameId());
        Player player = findPlayer(game, request.getPlayerId());

        if (player == null) {
            throw new InvalidMoveException("Player not found in this game!");
        }

        if (game.getState() != GameState.WAITING) {
            throw new InvalidMoveException("Cannot place ships after game has started!");
        }

        Ship ship = gameMapper.toShip(request);
        Coordinate coordinate = gameMapper.toCoordinate(request.getX(), request.getY());
        Orientation orientation = gameMapper.toOrientation(request.getOrientation());

        player.getBoard().placeShip(ship, coordinate, orientation);

        GameStateResponse response = gameMapper.toGameStateResponse(game, request.getPlayerId());
        response.setGameId(request.getGameId());

        return response;
    }

    @Override
    public AttackResultResponse attack(AttackRequest request) {
        Game game = getGame(request.getGameId());
        Player player = findPlayer(game, request.getPlayerId());

        if (player == null) {
            throw new InvalidMoveException("Player not found in this game!");
        }

        if (game.getState() != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is not in progress!");
        }

        if (!game.getCurrentPlayer().equals(player)) {
            throw new InvalidMoveException("It's not your turn!");
        }

        Coordinate coordinate = gameMapper.toCoordinate(request.getX(), request.getY());
        AttackResult result = game.attack(coordinate);

        return gameMapper.toAttackResultResponse(result, coordinate, game);
    }

    @Override
    public GameStateResponse getGameState(String gameId, String playerId) {
        Game game = getGame(gameId);

        GameStateResponse response = gameMapper.toGameStateResponse(game, playerId);
        response.setGameId(gameId);

        return response;
    }

    @Override
    public List<GameStateResponse> listActiveGames() {
        List<GameStateResponse> activeGames = new ArrayList<>();

        for (Map.Entry<String, Game> entry : games.entrySet()) {
            Game game = entry.getValue();
            if (game.getState() != GameState.FINISHED) {
                GameStateResponse response = gameMapper.toGameStateResponse(game, "");
                response.setGameId(entry.getKey());
                activeGames.add(response);
            }
        }

        return activeGames;
    }

    @Override
    public void deleteGame(String gameId) {
        games.remove(gameId);
    }

    private Game getGame(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }
        return game;
    }

    private Player findPlayer(Game game, String playerId) {
        if (game.getPlayer1().getId().equals(playerId)) {
            return game.getPlayer1();
        }
        if (game.getPlayer2() != null && game.getPlayer2().getId().equals(playerId)) {
            return game.getPlayer2();
        }
        return null;
    }
}
