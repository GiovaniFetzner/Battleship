package com.example.battleship.dto;

import com.example.battleship.dto.rest.inbound.JoinGameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JoinGameRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        JoinGameRequest joinGameRequest = new JoinGameRequest("game1", "Bob");

        String json = objectMapper.writeValueAsString(joinGameRequest);
        JoinGameRequest deserialized = objectMapper.readValue(json, JoinGameRequest.class);

        assertEquals(joinGameRequest.getGameId(), deserialized.getGameId());
        assertEquals(joinGameRequest.getPlayerName(), deserialized.getPlayerName());
    }
}
