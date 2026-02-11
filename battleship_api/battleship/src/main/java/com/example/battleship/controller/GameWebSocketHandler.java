package com.example.battleship.controller;

import com.example.battleship.dto.inbound.*;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("Player connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GameBaseMessageRequest gameMessage =
                objectMapper.readValue(message.getPayload(), GameBaseMessageRequest.class);

        if (gameMessage instanceof PlaceShipRequest placeShip) {
            handlePlaceShip(session, placeShip);

        } else if (gameMessage instanceof AttackRequest attack) {
            handleAttack(session, attack);

        } else if (gameMessage instanceof JoinGameByCodeRequest joinByCode) {
            handleJoinGameByCode(session, joinByCode);

        } else if (gameMessage instanceof JoinGameBaseRequest joinGame) {
            handleJoinGame(session, joinGame);

        } else {
            System.out.println("Untracked message: " + gameMessage.getClass().getSimpleName());
        }
    }

    private void handlePlaceShip(WebSocketSession session, PlaceShipRequest request) {
        System.out.println("Placing the ship " + request.getShipName() +
                " into (" + request.getX() + "," + request.getY() + ")");
    }

    private void handleAttack(WebSocketSession session, AttackRequest request) {
        System.out.println("Attack in (" + request.getX() + "," + request.getY() + ")");
    }

    private void handleJoinGame(WebSocketSession session, JoinGameBaseRequest request) {
        System.out.println("Player " + request.getPlayerName()
                + " is creating a new game. Session: " + session.getId());
    }

    private void handleJoinGameByCode(WebSocketSession session, JoinGameByCodeRequest request) {
        System.out.println("Player " + request.getPlayerName()
                + " is joining room " + request.getRoomCode()
                + ". Session: " + session.getId());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("Player disconnected: " + session.getId());
    }
}