package com.example.battleship.dto;

import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.example.battleship.dto.rest.outbound.ShipResponse;
import com.example.battleship.dto.rest.outbound.AttackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        GameStateResponse gameStateResponse = new GameStateResponse();
        gameStateResponse.setGameId("game1");
        gameStateResponse.setGameStatus("IN_PROGRESS");
        gameStateResponse.setPlayer1Id("player1");
        gameStateResponse.setPlayer1Name("Alice");
        gameStateResponse.setPlayer2Id("player2");
        gameStateResponse.setPlayer2Name("Bob");
        gameStateResponse.setCurrentPlayer("player1");
        gameStateResponse.setMyTurn(true);
        gameStateResponse.setWinner(null);
        gameStateResponse.setTurnNumber(5);
        gameStateResponse.setMyShipsRemaining(3);
        gameStateResponse.setOpponentShipsRemaining(2);
        gameStateResponse.setMyShips(List.of(new ShipResponse()));
        gameStateResponse.setMyAttacks(List.of(new AttackResponse()));
        gameStateResponse.setOpponentAttacks(List.of(new AttackResponse()));

        String json = objectMapper.writeValueAsString(gameStateResponse);
        GameStateResponse deserialized = objectMapper.readValue(json, GameStateResponse.class);

        assertEquals(gameStateResponse.getGameId(), deserialized.getGameId());
        assertEquals(gameStateResponse.getGameStatus(), deserialized.getGameStatus());
        assertEquals(gameStateResponse.getPlayer1Id(), deserialized.getPlayer1Id());
        assertEquals(gameStateResponse.getPlayer1Name(), deserialized.getPlayer1Name());
        assertEquals(gameStateResponse.getPlayer2Id(), deserialized.getPlayer2Id());
        assertEquals(gameStateResponse.getPlayer2Name(), deserialized.getPlayer2Name());
        assertEquals(gameStateResponse.getCurrentPlayer(), deserialized.getCurrentPlayer());
        assertEquals(gameStateResponse.isMyTurn(), deserialized.isMyTurn());
        assertEquals(gameStateResponse.getTurnNumber(), deserialized.getTurnNumber());
        assertEquals(gameStateResponse.getMyShipsRemaining(), deserialized.getMyShipsRemaining());
        assertEquals(gameStateResponse.getOpponentShipsRemaining(), deserialized.getOpponentShipsRemaining());
    }
}
