package com.example.battleship.dto.inbound;

public class PlaceShipRequest extends GameMessage {

    private String gameId;
    private String playerName;
    private String shipName;
    private int shipSize;
    private int x;
    private int y;
    private String orientation; // "HORIZONTAL" ou "VERTICAL"

    public PlaceShipRequest() {
    }

    public PlaceShipRequest(String gameId, String playerId, String shipName, int shipSize,
                            int x, int y, String orientation) {
        this.gameId = gameId;
        this.playerName = playerId;
        this.shipName = shipName;
        this.shipSize = shipSize;
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public int getShipSize() {
        return shipSize;
    }

    public void setShipSize(int shipSize) {
        this.shipSize = shipSize;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLACE_SHIP;
    }
}