package com.example.battleship.mapper;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.example.battleship.dto.rest.outbound.ShipResponse;
import com.example.battleship.dto.webSocket.outbound.AttackResultResponse;
import com.example.battleship.exception.InvalidMoveException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameMapper {

    public Coordinate toCoordinate(int x, int y) {
        return new Coordinate(x, y);
    }

    public Orientation toOrientation(String orientation) {
        if (orientation == null) {
            return Orientation.HORIZONTAL;
        }
        return Orientation.valueOf(orientation.toUpperCase());
    }

    public AttackResultResponse toAttackResultResponse(AttackResult result, Coordinate coordinate,
                                                       Game game) {

        return new AttackResultResponse(
                game.getId(),
                result,
                game.getCurrentPlayer().getName(),
                coordinate.getX(),
                coordinate.getY(),
                game.isGameOver(),
                game.getWinner() != null ? game.getWinner().getName() : null
        );
    }

    public GameStateResponse toGameStateResponse(Game game, String playerId) {
        GameStateResponse response = new GameStateResponse();

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        response.setGameStatus(mapGameStatus(game.getState()));
        response.setTurnNumber(game.getTurnCounter());

        response.setPlayer1Id(game.getPlayer1().getName());
        response.setPlayer1Name(game.getPlayer1().getName());
        
        if (game.getPlayer2() != null) {
            response.setPlayer2Id(game.getPlayer2().getName());
            response.setPlayer2Name(game.getPlayer2().getName());
        }

        if (game.getCurrentPlayer() != null) {
            response.setCurrentPlayer(game.getCurrentPlayer().getName());
            response.setMyTurn(game.getCurrentPlayer().getName().equals(playerId));
        }

        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getName());
        }

        Player currentPlayer = getPlayerById(game, playerId);
        if (currentPlayer != null) {
            response.setMyShips(toShipResponses(currentPlayer.getShips()));
            response.setMyShipsRemaining(currentPlayer.getAliveShipsCount());
            
            Player opponent = getOpponent(game, currentPlayer);
            if (opponent != null) {
                response.setOpponentShipsRemaining(opponent.getAliveShipsCount());
            }
        }

        return response;
    }

    private String mapGameStatus(GameState gameState) {
        return switch (gameState) {
            case WAITING -> "WAITING_FOR_PLAYERS";
            case IN_PROGRESS -> "PLAYING";
            case FINISHED -> "FINISHED";
            default -> gameState.name();
        };
    }

    private Player getOpponent(Game game, Player player) {
        if (game.getPlayer1().equals(player)) {
            return game.getPlayer2();
        } else {
            return game.getPlayer1();
        }
    }

    private Player getPlayerById(Game game, String playerId) {
        if (game.getPlayer1() != null && playerId != null && playerId.equals(game.getPlayer1().getName())) {
            return game.getPlayer1();
        }
        if (game.getPlayer2() != null && playerId != null && playerId.equals(game.getPlayer2().getName())) {
            return game.getPlayer2();
        }
        return null;
    }

    private List<ShipResponse> toShipResponses(List<Ship> ships) {
        List<ShipResponse> responses = new ArrayList<>();

        for (Ship ship : ships) {
            ShipResponse response = new ShipResponse();
            response.setType(ship.getName());
            response.setSize(ship.getSize());
            response.setHits(ship.getHits());
            ship.setDestroyed(response.isDestroyed());
            responses.add(response);
        }

        return responses;
    }

}