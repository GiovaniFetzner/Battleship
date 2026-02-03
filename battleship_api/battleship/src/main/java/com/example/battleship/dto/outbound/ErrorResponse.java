package com.example.battleship.dto.outbound;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDateTime;

@JsonTypeName("ERROR")
public class ErrorResponse extends GameBaseMessageResponse {
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String getType() { return "ERROR"; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}