package com.example.battleship.dto.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinGameBaseRequest.class, name = "JOIN_GAME"),
        @JsonSubTypes.Type(value = JoinGameByCodeRequest.class, name = "JOIN_GAME_BY_CODE"),
        @JsonSubTypes.Type(value = PlaceShipRequest.class, name = "PLACE_SHIP"),
        @JsonSubTypes.Type(value = AttackRequest.class, name = "ATTACK")
})
public abstract class GameBaseMessageRequest {
}