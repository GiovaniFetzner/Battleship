package com.example.battleship.dto;

import com.example.battleship.dto.rest.outbound.AttackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        AttackResponse attackResponse = new AttackResponse(3, 4, true, false);

        String json = objectMapper.writeValueAsString(attackResponse);
        AttackResponse deserialized = objectMapper.readValue(json, AttackResponse.class);

        assertEquals(attackResponse.getX(), deserialized.getX());
        assertEquals(attackResponse.getY(), deserialized.getY());
        assertEquals(attackResponse.isHit(), deserialized.isHit());
        assertEquals(attackResponse.isDestroyed(), deserialized.isDestroyed());
    }
}
