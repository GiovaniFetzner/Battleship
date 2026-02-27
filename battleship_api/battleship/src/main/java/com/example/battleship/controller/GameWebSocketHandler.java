package com.example.battleship.controller;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.webSocket.GameEventBroadcasterImpl;
import com.example.battleship.dto.webSocket.inbound.GameMessage;
import com.example.battleship.dto.webSocket.outbound.*;
import com.example.battleship.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final ObjectMapper objectMapper;
    private final GameEventBroadcasterImpl broadcaster;

    public GameWebSocketHandler(GameService gameService,
                                ObjectMapper objectMapper,
                                GameEventBroadcasterImpl broadcaster) {
        this.gameService = gameService;
        this.objectMapper = objectMapper;
        this.broadcaster = broadcaster;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String query = session.getUri().getQuery();
        String gameId = null;
        String playerName = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].equals("gameId")) {
                        gameId = keyValue[1];
                    } else if (keyValue[0].equals("playerName")) {
                        playerName = keyValue[1];
                    }
                }
            }
        }

        if (gameId == null || playerName == null) {
            session.close();
            return;
        }

        broadcaster.register(gameId, playerName, session);

        System.out.println("Registrado via conexão:");
        System.out.println("gameId=" + gameId);
        System.out.println("playerName=" + playerName);
        System.out.println("sessionId=" + session.getId());
    }


    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) throws Exception {

        GameMessage baseMessage = null;

        try {

            baseMessage = objectMapper.readValue(
                    message.getPayload(),
                    GameMessage.class
            );

            if (baseMessage.getType() == null) {
                throw new IllegalArgumentException("Message type is required");
            }

            broadcaster.register(
                    baseMessage.getGameId(),
                    baseMessage.getPlayerName(),
                    session
            );

            switch (baseMessage.getType()) {

                case ATTACK -> handleAttack(baseMessage);

                case PLACE_SHIP -> handlePlaceShip(baseMessage);

                case PLAYER_READY -> handlePlayerReady(baseMessage);

                default -> throw new IllegalArgumentException("Unknown message type");
            }

        } catch (Exception e) {

            String gameId = baseMessage != null ? baseMessage.getGameId() : null;

            ErrorResponse error =
                    new ErrorResponse(gameId, e.getMessage());

            session.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(error))
            );
        }
    }

    private void handleAttack(GameMessage message) throws Exception {

        if (message.getX() == null || message.getY() == null) {
            throw new IllegalArgumentException("Coordinates are required for ATTACK");
        }

        AttackResult result =
                gameService.attack(
                        message.getGameId(),
                        message.getPlayerName(),
                        message.getX(),
                        message.getY()
                );

        AttackResultResponse response =
                new AttackResultResponse(
                        message.getGameId(),
                        result,
                        gameService.getCurrentPlayer(message.getGameId()),
                        message.getX(),
                        message.getY(),
                        gameService.isGameOver(message.getGameId()),
                        gameService.getWinner(message.getGameId())
                );

        broadcaster.broadcast(message.getGameId(), response);
    }

    private void handlePlaceShip(GameMessage message) throws Exception {

        if (message.getShipType() == null ||
                message.getSize() == null ||
                message.getX() == null ||
                message.getY() == null ||
                message.getOrientation() == null) {

            throw new IllegalArgumentException("Invalid PLACE_SHIP payload");
        }

        gameService.placeShip(
                message.getGameId(),
                message.getPlayerName(),
                message.getShipType(),
                message.getSize(),
                message.getX(),
                message.getY(),
                message.getOrientation()
        );

        ShipPlacedResponse response =
                new ShipPlacedResponse(
                        message.getGameId(),
                        message.getPlayerName()
                );

        broadcaster.broadcast(message.getGameId(), response);
    }

    private void handlePlayerReady(GameMessage message) throws Exception {

        boolean bothReady =
                gameService.confirmPlayerReady(
                        message.getGameId(),
                        message.getPlayerName()
                );

        PlayerReadyResponse response =
                new PlayerReadyResponse(
                        message.getGameId(),
                        message.getPlayerName(),
                        bothReady
                );

        broadcaster.broadcast(message.getGameId(), response);

        if (bothReady) {

            String firstPlayer =
                    gameService.getCurrentPlayer(message.getGameId());

            GameStartResponse startResponse =
                    new GameStartResponse(
                            message.getGameId(),
                            firstPlayer
                    );

            broadcaster.broadcast(message.getGameId(), startResponse);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {

        broadcaster.removeSession(session);

        System.out.println("Player disconnected: " + session.getId());
    }
}
