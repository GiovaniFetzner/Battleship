package com.example.battleship.mapper;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.GameState;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.dto.outbound.ShipDTO;
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

    public Ship toShip(PlaceShipRequest request) {
        return new Ship(request.getShipName(), request.getShipSize());
    }

    public AttackResultResponse toAttackResultResponse(AttackResult result, Coordinate coordinate,
                                                       Game game) {
        AttackResultResponse response = new AttackResultResponse();
        response.setResult(result.name());
        response.setX(coordinate.getX());
        response.setY(coordinate.getY());
        response.setCurrentPlayer(game.getCurrentPlayer().getId());
        response.setGameOver(game.isGameOver());

        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getId());
        }

        return response;
    }

    public GameStateResponse toGameStateResponse(Game game, String playerId) {
        GameStateResponse response = new GameStateResponse();

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        response.setGameStatus(mapGameStatus(game.getState()));
        response.setTurnNumber(game.getTurnCounter());

        response.setPlayer1Id(game.getPlayer1().getId());
        response.setPlayer1Name(game.getPlayer1().getId());
        
        if (game.getPlayer2() != null) {
            response.setPlayer2Id(game.getPlayer2().getId());
            response.setPlayer2Name(game.getPlayer2().getId());
        }

        if (game.getCurrentPlayer() != null) {
            response.setCurrentPlayer(game.getCurrentPlayer().getId());
            response.setMyTurn(game.getCurrentPlayer().getId().equals(playerId));
        }

        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getId());
        }

        Player currentPlayer = getPlayerById(game, playerId);
        if (currentPlayer != null) {
            response.setMyShips(toShipDTOs(currentPlayer.getShips()));
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
        if (game.getPlayer1() != null && playerId != null && playerId.equals(game.getPlayer1().getId())) {
            return game.getPlayer1();
        }
        if (game.getPlayer2() != null && playerId != null && playerId.equals(game.getPlayer2().getId())) {
            return game.getPlayer2();
        }
        return null;
    }

    private List<ShipDTO> toShipDTOs(List<Ship> ships) {
        List<ShipDTO> shipDTOs = new ArrayList<>();

        for (Ship ship : ships) {
            ShipDTO dto = new ShipDTO();
            dto.setName(ship.getName());
            dto.setSize(ship.getSize());
            dto.setHits(ship.getHits());
            dto.setDestroyed(ship.getHits() >= ship.getSize());
            shipDTOs.add(dto);
        }

        return shipDTOs;
    }
}