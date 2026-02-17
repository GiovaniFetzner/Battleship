package com.example.battleship.controller;

import com.example.battleship.domain.map.AttackResult;
import com.example.battleship.dto.webSocket.inbound.GameMessage;
import com.example.battleship.dto.webSocket.outbound.*;
import com.example.battleship.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, WebSocketSession>> gameSessions =
            new ConcurrentHashMap<>();


    public GameWebSocketHandler(GameService gameService, ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Player connected: " + session.getId());
    }

    private void registerSession(String gameId, String playerName, WebSocketSession session) {

        gameSessions
                .computeIfAbsent(gameId, g -> new ConcurrentHashMap<>())
                .put(playerName, session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        GameMessage baseMessage = null;

        try {

            baseMessage =
                    objectMapper.readValue(message.getPayload(), GameMessage.class);

            if (baseMessage.getType() == null) {
                throw new IllegalArgumentException("Message type is required");
            }

            switch (baseMessage.getType()) {

                case ATTACK:
                    handleAttack(session, baseMessage);
                    break;

                case PLACE_SHIP:
                    handlePlaceShip(session, baseMessage);
                    break;

                case PLAYER_READY:
                    handlePlayerReady(session, baseMessage);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown message type");
            }

        } catch (Exception e) {

            String gameId = baseMessage != null ? baseMessage.getGameId() : null;

            sendError(session, gameId, e.getMessage());
        }
    }

    private void sendError(WebSocketSession session,
                           String gameId,
                           String errorMessage) throws Exception {

        ErrorResponse error =
                new ErrorResponse(gameId, errorMessage);

        String json = objectMapper.writeValueAsString(error);

        session.sendMessage(new TextMessage(json));
    }



    private void handleAttack(WebSocketSession session, GameMessage message) throws Exception {

        registerSession(message.getGameId(), message.getPlayerId(), session);

        if (message.getX() == null || message.getY() == null) {
            throw new IllegalArgumentException("Coordinates are required for ATTACK");
        }

        AttackResult result =
                gameService.attack(
                        message.getGameId(),
                        message.getPlayerId(),
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

        broadcastToGame(message.getGameId(), response);
    }

    private void handlePlaceShip(WebSocketSession session, GameMessage message) throws Exception {

        registerSession(message.getGameId(), message.getPlayerId(), session);

        if (message.getShipType() == null ||
                message.getSize() == null ||
                message.getX() == null ||
                message.getY() == null ||
                message.getOrientation() == null) {

            throw new IllegalArgumentException("Invalid PLACE_SHIP payload");
        }

        gameService.placeShip(
                message.getGameId(),
                message.getPlayerId(),
                message.getShipType(),
                message.getSize(),
                message.getX(),
                message.getY(),
                message.getOrientation()
        );

        ShipPlacedResponse response =
                new ShipPlacedResponse(
                        message.getGameId(),
                        message.getPlayerId()
                );

        broadcastToGame(message.getGameId(), response);

    }


    private void handlePlayerReady(WebSocketSession session, GameMessage message) throws Exception {

        registerSession(message.getGameId(), message.getPlayerId(), session);

        boolean bothReady =
                gameService.markPlayerReady(
                        message.getGameId(),
                        message.getPlayerId()
                );

        PlayerReadyResponse response =
                new PlayerReadyResponse(
                        message.getGameId(),
                        message.getPlayerId(),
                        bothReady
                );

        broadcastToGame(message.getGameId(), response);

        if (bothReady) {

            String firstPlayer =
                    gameService.getCurrentPlayer(message.getGameId());

            GameStartResponse startResponse =
                    new GameStartResponse(
                            message.getGameId(),
                            firstPlayer
                    );

            broadcastToGame(message.getGameId(), startResponse);
        }
    }

    private void broadcastToGame(String gameId, GameEvent event) throws Exception {

        Map<String, WebSocketSession> gameSessionMap =
                gameSessions.get(gameId);

        if (gameSessionMap == null) return;

        String json = objectMapper.writeValueAsString(event);

        for (WebSocketSession ws : gameSessionMap.values()) {
            ws.sendMessage(new TextMessage(json));
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        gameSessions.values().forEach(playerMap ->
                playerMap.values().removeIf(ws -> ws.getId().equals(session.getId()))
        );

        System.out.println("Player disconnected: " + session.getId());
    }

}
