package com.example.battleship.mapper;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.dto.outbound.ShipDTO;
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
        response.setCurrentPlayer(game.getCurrentPlayer().getName());
        response.setGameOver(game.isGameOver());

        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getName());
        }

        return response;
    }

    public GameStateResponse toGameStateResponse(Game game, String playerId) {
        GameStateResponse response = new GameStateResponse();

        response.setState(game.getState().name());
        response.setPlayer1(game.getPlayer1().getName());

        if (game.getPlayer2() != null) {
            response.setPlayer2(game.getPlayer2().getName());
        }

        if (game.getCurrentPlayer() != null) {
            response.setCurrentPlayer(game.getCurrentPlayer().getName());
        }

        response.setTurnNumber(game.getTurnCounter());
        response.setGameOver(game.isGameOver());

        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getName());
        }

        Player currentPlayer = getPlayerById(game, playerId);
        if (currentPlayer != null) {
            response.setMyShips(toShipDTOs(currentPlayer.getShips()));
        }

        return response;
    }

    private Player getPlayerById(Game game, String playerId) {
        if (game.getPlayer1().getName().equals(playerId)) {
            return game.getPlayer1();
        }
        if (game.getPlayer2() != null && game.getPlayer2().getName().equals(playerId)) {
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
