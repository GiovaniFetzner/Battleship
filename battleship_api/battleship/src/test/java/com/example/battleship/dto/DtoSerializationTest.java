package com.example.battleship.dto;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.GameBaseMessageRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldResolveJoinGameBaseRequestSerialization() throws Exception {
        JoinGameBaseRequest request = new JoinGameBaseRequest("Player1");

        String json = objectMapper.writeValueAsString(request);

        GameBaseMessageRequest deserialized =
                objectMapper.readValue(json, GameBaseMessageRequest.class);

        assertTrue(deserialized instanceof JoinGameBaseRequest);

        JoinGameBaseRequest join = (JoinGameBaseRequest) deserialized;
        assertEquals("Player1", join.getPlayerName());
    }


    @Test
    void shouldResolvePlaceShipRequestSerialization() throws Exception {
        PlaceShipRequest request =
                new PlaceShipRequest("gameId", "playerId", "Battleship", 4, 1, 1, "HORIZONTAL");

        String json = objectMapper.writeValueAsString(request);

        GameBaseMessageRequest deserialized =
                objectMapper.readValue(json, GameBaseMessageRequest.class);

        assertTrue(deserialized instanceof PlaceShipRequest);

        PlaceShipRequest placeShip = (PlaceShipRequest) deserialized;

        assertEquals(request.getGameId(), placeShip.getGameId());
        assertEquals(request.getPlayerId(), placeShip.getPlayerId());
        assertEquals(request.getShipName(), placeShip.getShipName());
        assertEquals(request.getShipSize(), placeShip.getShipSize());
        assertEquals(request.getX(), placeShip.getX());
        assertEquals(request.getY(), placeShip.getY());
        assertEquals(request.getOrientation(), placeShip.getOrientation());
    }


    @Test
    void shouldResolveAttackRequestSerialization() throws Exception {
        AttackRequest request = new AttackRequest("gameId", "playerId", 2, 3);

        String json = objectMapper.writeValueAsString(request);

        GameBaseMessageRequest deserialized =
                objectMapper.readValue(json, GameBaseMessageRequest.class);

        assertTrue(deserialized instanceof AttackRequest);

        AttackRequest attack = (AttackRequest) deserialized;

        assertEquals(request.getGameId(), attack.getGameId());
        assertEquals(request.getPlayerId(), attack.getPlayerId());
        assertEquals(request.getX(), attack.getX());
        assertEquals(request.getY(), attack.getY());
    }


    @Test
    void shouldResolveGameStateResponseSerialization() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId("gameId");
        response.setGameStatus("WAITING");
        response.setPlayer1Id("player1Id");
        response.setPlayer1Name("Player1");
        response.setPlayer2Id("player2Id");
        response.setPlayer2Name("Player2");

        String json = objectMapper.writeValueAsString(response);

        GameStateResponse deserialized =
                objectMapper.readValue(json, GameStateResponse.class);

        assertEquals(response.getGameId(), deserialized.getGameId());
        assertEquals(response.getGameStatus(), deserialized.getGameStatus());
        assertEquals(response.getPlayer1Id(), deserialized.getPlayer1Id());
        assertEquals(response.getPlayer1Name(), deserialized.getPlayer1Name());
        assertEquals(response.getPlayer2Id(), deserialized.getPlayer2Id());
        assertEquals(response.getPlayer2Name(), deserialized.getPlayer2Name());
    }

    @Test
    void shouldResolveRequestDTOTypeByJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AttackRequest attack = new AttackRequest("gameId", "playerId", 1, 2);

        String json = mapper.writeValueAsString(attack);

        GameBaseMessageRequest base =
                mapper.readValue(json, GameBaseMessageRequest.class);

        assertTrue(base instanceof AttackRequest);

        AttackRequest resolved = (AttackRequest) base;
        assertEquals("gameId", resolved.getGameId());
        assertEquals("playerId", resolved.getPlayerId());
        assertEquals(1, resolved.getX());
        assertEquals(2, resolved.getY());
    }



}
