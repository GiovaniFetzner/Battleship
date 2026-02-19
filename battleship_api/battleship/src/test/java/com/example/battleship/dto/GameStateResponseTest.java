package com.example.battleship.dto;

import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        GameStateResponse gameStateResponse = new GameStateResponse();
        gameStateResponse.setGameId("game1");
        gameStateResponse.setGameStatus("IN_PROGRESS");
        gameStateResponse.setPlayer1Name("Alice");
        gameStateResponse.setPlayer2Name("Bob");
        gameStateResponse.setCurrentPlayer("player1");
        gameStateResponse.setMyTurn(true);
        gameStateResponse.setWinner(null);
        gameStateResponse.setTurnNumber(5);
        gameStateResponse.setMyShipsRemaining(3);
        gameStateResponse.setOpponentShipsRemaining(2);

        String json = objectMapper.writeValueAsString(gameStateResponse);
        GameStateResponse deserialized = objectMapper.readValue(json, GameStateResponse.class);

        assertEquals(gameStateResponse.getGameId(), deserialized.getGameId());
        assertEquals(gameStateResponse.getGameStatus(), deserialized.getGameStatus());
        assertEquals(gameStateResponse.getPlayer1Name(), deserialized.getPlayer1Name());
        assertEquals(gameStateResponse.getPlayer2Name(), deserialized.getPlayer2Name());
        assertEquals(gameStateResponse.getCurrentPlayer(), deserialized.getCurrentPlayer());
        assertEquals(gameStateResponse.isMyTurn(), deserialized.isMyTurn());
        assertEquals(gameStateResponse.getTurnNumber(), deserialized.getTurnNumber());
        assertEquals(gameStateResponse.getMyShipsRemaining(), deserialized.getMyShipsRemaining());
        assertEquals(gameStateResponse.getOpponentShipsRemaining(), deserialized.getOpponentShipsRemaining());
    }
}
