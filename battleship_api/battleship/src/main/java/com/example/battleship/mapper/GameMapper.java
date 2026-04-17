package com.example.battleship.mapper;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Board;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.domain.map.Ship;
import com.example.battleship.dto.rest.outbound.CellResponse;
import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.example.battleship.dto.rest.outbound.ShipStatusResponse;
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
                coordinate.x(),
                coordinate.y(),
                game.isGameOver(),
                game.getWinner() != null ? game.getWinner().getName() : null
        );
    }

    public GameStateResponse toGameStateResponse(Game game, String playerName) {

        if (game == null) {
            throw new InvalidMoveException("Game not found!");
        }

        GameStateResponse response = new GameStateResponse();

        response.setGameStatus(game.getState().name());
        response.setTurnNumber(game.getTurnCounter());

        // Player 1
        if (game.getPlayer1() != null) {
            response.setPlayer1Name(game.getPlayer1().getName());
        }

        // Player 2
        if (game.getPlayer2() != null) {
            response.setPlayer2Name(game.getPlayer2().getName());
        }

        // Current Player
        if (game.getCurrentPlayer() != null) {
            String currentName = game.getCurrentPlayer().getName();
            response.setCurrentPlayer(currentName);
            response.setMyTurn(currentName.equals(playerName));
        }

        // Winner
        if (game.getWinner() != null) {
            response.setWinner(game.getWinner().getName());
        }

        Player me = getPlayerByName(game, playerName);

        if (me != null) {
            Board myBoard = me.getBoard();

            List<ShipStatusResponse> myShips = toShipStatus(myBoard);
            response.setMyShips(myShips);
            response.setMyShipsRemaining((int) myShips.stream().filter(ship -> !ship.isDestroyed()).count());
            response.setMyBoardCells(toBoardCells(myBoard));

            Player opponent = getOpponent(game, me);

            if (opponent != null) {
                Board opponentBoard = opponent.getBoard();
                response.setOpponentBoardCells(toBoardCells(opponentBoard));
                response.setOpponentShipsRemaining((int) toShipStatus(opponentBoard).stream()
                        .filter(ship -> !ship.isDestroyed())
                        .count());
                response.setMyAttacksCount((int) toBoardCells(opponentBoard).stream()
                        .filter(CellResponse::isAttacked)
                        .count());
            }
        }

        return response;
    }


    private Player getOpponent(Game game, Player player) {
        if (player == null) return null;

        if (player.equals(game.getPlayer1())) {
            return game.getPlayer2();
        }
        return game.getPlayer1();
    }

    private Player getPlayerByName(Game game, String playerName) {
        if (playerName == null) return null;

        if (game.getPlayer1() != null &&
                playerName.equals(game.getPlayer1().getName())) {
            return game.getPlayer1();
        }

        if (game.getPlayer2() != null &&
                playerName.equals(game.getPlayer2().getName())) {
            return game.getPlayer2();
        }

        return null;
    }

    private List<ShipStatusResponse> toShipStatus(Board board) {
        List<ShipStatusResponse> ships = new ArrayList<>();
        for (Ship ship : board.getShips()) {
            ships.add(new ShipStatusResponse(
                    ship.getName(),
                    ship.getSize(),
                    ship.getHits(),
                    ship.isDestroyed(),
                    true
            ));
        }
        return ships;
    }

    private List<CellResponse> toBoardCells(Board board) {
        List<CellResponse> cells = new ArrayList<>();
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                boolean attacked = board.isAttackedAt(x, y);
                boolean hasShip = board.hasShipAt(x, y);
                cells.add(new CellResponse(x, y, attacked, attacked && hasShip, hasShip));
            }
        }
        return cells;
    }
}