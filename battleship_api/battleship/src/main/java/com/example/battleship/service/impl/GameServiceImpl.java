package com.example.battleship.service.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.game.ShipFactory;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.exception.InvalidMoveException;
import com.example.battleship.repository.GameRepository;
import com.example.battleship.service.GameService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(String playerName) {

        Player player1 = new Player(playerName);
        player1.setShips(ShipFactory.createDefaultShips());

        Game game = new Game(player1, null);

        gameRepository.save(game);

        return game;
    }

    @Override
    public Game joinGame(String gameId, String playerName) {

        Game game = getGameOrThrow(gameId);

        if (game.getPlayer2() != null) {
            throw new InvalidMoveException("Game is already full!");
        }

        Player player2 = new Player(playerName);
        player2.setShips(ShipFactory.createDefaultShips());

        game.setPlayer2(player2);

        gameRepository.save(game);

        return game;
    }


    @Override
    public Game startGame(String gameId) {

        Game game = getGameOrThrow(gameId);

        // Domain should validate if both players are present and ready
        game.start();

        gameRepository.save(game);

        return game;
    }


    @Override
    public Game placeShip(String gameId,
                          String playerName,
                          String shipType,
                          int size,
                          int x,
                          int y,
                          String orientation) {

        Game game = getGameOrThrow(gameId);
        Player player = game.findPlayer(game, playerName);

        if (!game.canPlaceShips()) {
            throw new InvalidMoveException("Cannot place ships now!");
        }

        Coordinate coordinate = new Coordinate(x, y);
        Orientation orient = Orientation.valueOf(orientation);

        Ship ship = new Ship(shipType, size);

        player.getBoard().placeShip(ship, coordinate, orient);

        gameRepository.save(game);

        return game;
    }


    @Override
    public AttackResult attack(String gameId,
                               String playerId,
                               int x,
                               int y) {

        Game game = getGameOrThrow(gameId);

        Player player = game.findPlayer(game, playerId);

        if (!game.getCurrentPlayer().equals(player)) {
            throw new InvalidMoveException("It's not your turn!");
        }

        AttackResult result = game.attack(new Coordinate(x, y));

        gameRepository.save(game);

        return result;
    }


    @Override
    public Game getGameState(String gameId) {
        return getGameOrThrow(gameId);
    }

    @Override
    public List<Game> listActiveGames() {
        return gameRepository.findAll()
                .values()
                .stream()
                .filter(game -> game.getState() != GameState.FINISHED)
                .toList();
    }

    @Override
    public void deleteGame(String gameId) {

        getGameOrThrow(gameId);

        gameRepository.deleteById(gameId);
    }


    private Game getGameOrThrow(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    public boolean markPlayerReady(String gameId, String playerName) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        game.markPlayerReady(playerName);

        return game.areBothPlayersReady();
    }

    @Override
    public String getCurrentPlayer(String gameId) {

        Game game = getGameOrThrow(gameId);

        return game.getCurrentPlayer().getName();
    }


    @Override
    public boolean isGameOver(String gameId) {

        Game game = getGameOrThrow(gameId);

        return game.getState() == GameState.FINISHED;
    }


    @Override
    public String getWinner(String gameId) {

        Game game = getGameOrThrow(gameId);

        Player winner = game.getWinner();

        return winner != null ? winner.getName() : null;
    }

}
