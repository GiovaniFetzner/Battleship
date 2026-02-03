package com.example.battleship.exception;

public class InsufficientPlayersException extends RuntimeException{
    public InsufficientPlayersException(String message) {
        super(message);
    }
}
