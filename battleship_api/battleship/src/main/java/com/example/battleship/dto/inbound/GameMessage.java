package com.example.battleship.dto.inbound;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class GameMessage {

    public abstract GameMessageType getType();
}