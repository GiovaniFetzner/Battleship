package com.example.battleship.controller;

import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.JoinGameByCodeRequest;
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
            @RequestBody JoinGameBaseRequest request) {

        return ResponseEntity.ok(gameApplicationService.createGame(request));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateResponse> joinGame(
            @PathVariable String gameId,
            @RequestBody JoinGameBaseRequest request) {

        return ResponseEntity.ok(gameApplicationService.joinGame(gameId, request));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(
            @PathVariable String gameId,
            @RequestParam String playerId) {

        return ResponseEntity.ok(gameApplicationService.getGameState(gameId, playerId));
    }

    @PostMapping("/join-by-code")
    public ResponseEntity<GameStateResponse> joinGameByCode(
            @RequestBody JoinGameByCodeRequest request) {

        return ResponseEntity.ok(gameApplicationService.joinGameByCode(request.getRoomCode(), request.getPlayerName()));
    }
}
