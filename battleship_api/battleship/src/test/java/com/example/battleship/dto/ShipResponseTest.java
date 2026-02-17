package com.example.battleship.dto;

import com.example.battleship.dto.rest.outbound.ShipResponse;
import com.example.battleship.dto.rest.outbound.CellResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationAndDeserialization() throws Exception {
        ShipResponse shipResponse = new ShipResponse();
        shipResponse.setShipId("ship1");
        shipResponse.setType("Battleship");
        shipResponse.setSize(4);
        shipResponse.setHits(2);
        shipResponse.setDestroyed(false);
        shipResponse.setCoordinates(List.of(new CellResponse(1, 1, false, false, true)));

        String json = objectMapper.writeValueAsString(shipResponse);
        ShipResponse deserialized = objectMapper.readValue(json, ShipResponse.class);

        assertEquals(shipResponse.getShipId(), deserialized.getShipId());
        assertEquals(shipResponse.getType(), deserialized.getType());
        assertEquals(shipResponse.getSize(), deserialized.getSize());
        assertEquals(shipResponse.getHits(), deserialized.getHits());
        assertEquals(shipResponse.isDestroyed(), deserialized.isDestroyed());
        assertEquals(shipResponse.getCoordinates().size(), deserialized.getCoordinates().size());
    }
}
