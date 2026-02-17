package com.example.battleship.dto.webSocket.outbound;

public class ErrorResponse extends GameEvent {

    private final String message;

    public ErrorResponse(String gameId,
                         String message) {

        super(GameEventType.ERROR, gameId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
