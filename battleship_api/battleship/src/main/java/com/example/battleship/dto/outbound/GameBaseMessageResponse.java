package com.example.battleship.dto.outbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShipPlacedResponse.class, name = "SHIP_PLACED"),
        @JsonSubTypes.Type(value = AttackResultResponse.class, name = "ATTACK_RESULT"),
        @JsonSubTypes.Type(value = GameStateResponse.class, name = "GAME_STATE"),
        @JsonSubTypes.Type(value = ErrorResponse.class, name = "ERROR")
})
public abstract class GameBaseMessageResponse {
    public abstract String getType();
}