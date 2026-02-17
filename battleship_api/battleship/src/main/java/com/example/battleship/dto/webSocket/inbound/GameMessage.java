package com.example.battleship.dto.webSocket.inbound;

public class GameMessage {

    /*
    * This class represents the structure of messages sent from the client to the server.
    * It includes a type field to indicate the kind of message (e.g., "JOIN_GAME", "PLAYER_READY", "ATTACK") and relevant data fields for all of those.
    * The server will use the type field to determine how to process the message and which service methods to call.
    *
    * ATTACK → x, y
    * PLACE_SHIP → shipType, size, orientation, x, y
    * PLAYER_READY → só gameId + playerId
     */

    private GameMessageType type;
    private String gameId;
    private String playerId;

    // ===== ATTACK =====
    private Integer x;
    private Integer y;

    // ===== PLACE_SHIP =====
    private String shipType;
    private Integer size;
    private String orientation;

    public GameMessage() {
    }

    public GameMessageType getType() {
        return type;
    }

    public void setType(GameMessageType type) {
        this.type = type;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}
