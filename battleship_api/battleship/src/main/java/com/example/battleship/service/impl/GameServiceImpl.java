package com.example.battleship.service.impl;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
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

        Game game = new Game(player1);

        gameRepository.save(game);

        return game;
    }


    @Override
    public Game joinGame(String gameId, String playerName) {

        Game game = getGameOrThrow(gameId);

        Player player2 = new Player(playerName);

        game.addPlayer2(player2);

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

        Coordinate coordinate = new Coordinate(x, y);
        Orientation orient = Orientation.valueOf(orientation);
        Ship ship = new Ship(shipType, size);

        game.placeShip(playerName, ship, coordinate, orient);

        gameRepository.save(game);

        return game;
    }

    @Override
    public AttackResult attack(String gameId,
                               String playerName,
                               int x,
                               int y) {

        Game game = getGameOrThrow(gameId);

        AttackResult result =
                game.attack(playerName, new Coordinate(x, y));

        gameRepository.save(game);

        return result;
    }

    @Override
    public boolean confirmPlayerReady(String gameId, String playerName) {

        Game game = getGameOrThrow(gameId);

        game.confirmPlacement(playerName);

        gameRepository.save(game);

        return game.getState() == GameState.IN_PROGRESS;
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
