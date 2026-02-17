package com.example.battleship.dto;

import com.example.battleship.dto.rest.inbound.CreateGameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateGameRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("Alice");

        String json = objectMapper.writeValueAsString(createGameRequest);
        CreateGameRequest deserialized = objectMapper.readValue(json, CreateGameRequest.class);

        assertEquals(createGameRequest.getPlayerName(), deserialized.getPlayerName());
    }
}
