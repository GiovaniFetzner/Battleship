package com.example.battleship.controller;

import com.example.battleship.dto.inbound.CreateGameRequest;
import com.example.battleship.dto.inbound.JoinGameRequest;
import com.example.battleship.dto.outbound.GameStateResponse;
import com.example.battleship.service.GameApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameApplicationService gameApplicationService;

    public GameController(GameApplicationService gameApplicationService) {
        this.gameApplicationService = gameApplicationService;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> createGame(
            @RequestBody CreateGameRequest request) {

        return ResponseEntity.ok(gameApplicationService.createGame(request));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(
            @PathVariable String gameId,
            @RequestParam String playerId) {

        return ResponseEntity.ok(gameApplicationService.getGameState(gameId, playerId));
    }

    @PostMapping("/join")
    public ResponseEntity<GameStateResponse> joinGameByCode(
            @RequestBody JoinGameRequest request) {

        return ResponseEntity.ok(gameApplicationService.joinGame(request.getGameId(), request.getPlayerName()));
    }
}
