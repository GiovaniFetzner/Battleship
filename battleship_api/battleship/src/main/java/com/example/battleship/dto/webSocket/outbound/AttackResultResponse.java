package com.example.battleship.dto.webSocket.outbound;

import com.example.battleship.domain.map.AttackResult;

public class AttackResultResponse extends GameEvent {

    private final AttackResult result;
    private final int x;
    private final int y;
    private final String currentPlayer;
    private final boolean gameOver;
    private final String winner;

    public AttackResultResponse(String gameId,
                                AttackResult result,
                                String nextPlayer,
                                int x,
                                int y,
                                boolean gameOver,
                                String winner)
    {

        super(GameEventType.ATTACK_RESULT, gameId);

        this.result = result;
        this.x = x;
        this.y = y;
        this.currentPlayer = nextPlayer;
        this.gameOver = gameOver;
        this.winner = winner;
    }

    public AttackResult getResult() { return result; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }

}