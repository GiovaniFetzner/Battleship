package com.example.battleship.mapper;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.game.Player;
import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.domain.map.Coordinate;
import com.example.battleship.domain.map.Orientation;
import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.example.battleship.dto.webSocket.outbound.AttackResultResponse;
import com.example.battleship.exception.InvalidMoveException;
import org.springframework.stereotype.Component;

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

            boolean allDestroyed = me.getBoard().allShipsDestroyed();

            response.setMyShipsRemaining(
                    allDestroyed ? 0 : 1
            );

            Player opponent = getOpponent(game, me);

            if (opponent != null) {
                response.setOpponentShipsRemaining(
                        opponent.getBoard().allShipsDestroyed() ? 0 : 1
                );
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



}