package com.example.battleship.dto.outbound;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("SHIP_PLACED")
public class ShipPlacedResponse extends GameBaseMessageResponse {
    private boolean success;
    private String message;
    private String shipName;

    public ShipPlacedResponse() {}

    public ShipPlacedResponse(boolean success, String message, String shipName) {
        this.success = success;
        this.message = message;
        this.shipName = shipName;
    }

    @Override
    public String getType() { return "SHIP_PLACED"; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }
}