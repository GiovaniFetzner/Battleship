package com.example.battleship.dto;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJoinGameBaseRequestSerialization() throws Exception {
        JoinGameBaseRequest request = new JoinGameBaseRequest("Player1");

        String json = objectMapper.writeValueAsString(request);
        JoinGameBaseRequest deserialized = objectMapper.readValue(json, JoinGameBaseRequest.class);

        assertEquals(request.getPlayerName(), deserialized.getPlayerName());
        assertEquals(request.getType(), deserialized.getType());
    }

    @Test
    void testPlaceShipRequestSerialization() throws Exception {
        PlaceShipRequest request = new PlaceShipRequest("gameId", "playerId", "Battleship", 4, 1, 1, "HORIZONTAL");

        String json = objectMapper.writeValueAsString(request);
        PlaceShipRequest deserialized = objectMapper.readValue(json, PlaceShipRequest.class);

        assertEquals(request.getGameId(), deserialized.getGameId());
        assertEquals(request.getPlayerId(), deserialized.getPlayerId());
        assertEquals(request.getShipName(), deserialized.getShipName());
        assertEquals(request.getShipSize(), deserialized.getShipSize());
        assertEquals(request.getX(), deserialized.getX());
        assertEquals(request.getY(), deserialized.getY());
        assertEquals(request.getOrientation(), deserialized.getOrientation());
    }

    @Test
    void testAttackRequestSerialization() throws Exception {
        AttackRequest request = new AttackRequest("gameId", "playerId", 2, 3);

        String json = objectMapper.writeValueAsString(request);
        AttackRequest deserialized = objectMapper.readValue(json, AttackRequest.class);

        assertEquals(request.getGameId(), deserialized.getGameId());
        assertEquals(request.getPlayerId(), deserialized.getPlayerId());
        assertEquals(request.getX(), deserialized.getX());
        assertEquals(request.getY(), deserialized.getY());
    }

    @Test
    void testGameStateResponseSerialization() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId("gameId");
        response.setGameStatus("WAITING");
        response.setPlayer1Id("player1Id");
        response.setPlayer1Name("Player1");
        response.setPlayer2Id("player2Id");
        response.setPlayer2Name("Player2");

        String json = objectMapper.writeValueAsString(response);
        GameStateResponse deserialized = objectMapper.readValue(json, GameStateResponse.class);

        assertEquals(response.getGameId(), deserialized.getGameId());
        assertEquals(response.getGameStatus(), deserialized.getGameStatus());
        assertEquals(response.getPlayer1Id(), deserialized.getPlayer1Id());
        assertEquals(response.getPlayer1Name(), deserialized.getPlayer1Name());
        assertEquals(response.getPlayer2Id(), deserialized.getPlayer2Id());
        assertEquals(response.getPlayer2Name(), deserialized.getPlayer2Name());
    }
}
