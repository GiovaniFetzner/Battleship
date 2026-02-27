package com.example.battleship.dto.webSocket.outbound;

public enum GameEventType {
    ATTACK_RESULT,
    GAME_STATE_UPDATED,
    PLAYER_READY,
    GAME_START,
    SHIP_PLACED,
    ERROR
}
