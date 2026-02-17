package com.example.battleship.dto;

import com.example.battleship.dto.rest.outbound.CellResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        CellResponse cellResponse = new CellResponse(1, 2, true, false, true);

        String json = objectMapper.writeValueAsString(cellResponse);
        CellResponse deserialized = objectMapper.readValue(json, CellResponse.class);

        assertEquals(cellResponse.getX(), deserialized.getX());
        assertEquals(cellResponse.getY(), deserialized.getY());
        assertEquals(cellResponse.isAttacked(), deserialized.isAttacked());
        assertEquals(cellResponse.isHit(), deserialized.isHit());
        assertEquals(cellResponse.isHasShip(), deserialized.isHasShip());
    }
}
