package com.example.battleship.dto;

import com.example.battleship.dto.outbound.GameStateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateResponseSerializationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerializeGameStateWithCorrectType() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId("gameId");

        String json = mapper.writeValueAsString(response);

        JsonNode node = mapper.readTree(json);
        assertEquals("GAME_STATE", node.get("type").asText());
    }
}

