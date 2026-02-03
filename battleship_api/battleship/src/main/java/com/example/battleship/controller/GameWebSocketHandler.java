package com.example.battleship.controller;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.GameBaseMessageRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Player connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        GameBaseMessageRequest gameMessage = objectMapper.readValue(payload, GameBaseMessageRequest.class);

        switch (gameMessage.getType()) {
            case "JOIN_GAME":
                handleJoinGame(session, (JoinGameBaseRequest) gameMessage);
                break;
            case "PLACE_SHIP":
                handlePlaceShip(session, (PlaceShipRequest) gameMessage);
                break;
            case "ATTACK":
                handleAttack(session, (AttackRequest) gameMessage);
                break;
            default:
                System.out.println("Untracked message type: " + gameMessage.getType());
        }
    }

    private void handleJoinGame(WebSocketSession session, JoinGameBaseRequest request) {
        System.out.println("Player " + request.getPlayerName() + " wants to join the game.");
    }

    private void handlePlaceShip(WebSocketSession session, PlaceShipRequest request) {
        System.out.println("Placing the ship " + request.getShipName() +
                " into (" + request.getX() + "," + request.getY() + ")");
    }

    private void handleAttack(WebSocketSession session, AttackRequest request) {
        System.out.println("Attack in (" + request.getX() + "," + request.getY() + ")");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Player disconnected: " + session.getId());
    }
}