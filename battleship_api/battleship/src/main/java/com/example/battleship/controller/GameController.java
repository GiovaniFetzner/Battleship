package com.example.battleship.controller;

import com.example.battleship.domain.game.Game;
import com.example.battleship.dto.rest.inbound.CreateGameRequest;
import com.example.battleship.dto.rest.inbound.JoinGameRequest;
import com.example.battleship.dto.rest.outbound.GameStateResponse;
import com.example.battleship.mapper.GameMapper;
import com.example.battleship.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    public GameController(GameService gameApplicationService, GameMapper gameMapper) {
        this.gameService = gameApplicationService;
        this.gameMapper = gameMapper;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> create(
            @RequestBody CreateGameRequest request) {

        Game game = gameService.createGame(request.getPlayerName());

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, request.getPlayerName());

        response.setGameId(game.getId());

        return ResponseEntity.ok(response);
    }



    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(
            @PathVariable String gameId,
            @RequestParam String playerId) {

        Game game = gameService.getGameState(gameId);

        GameStateResponse response = gameMapper.toGameStateResponse(game, playerId);

        response.setGameId(gameId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<GameStateResponse> joinGameByCode(
            @RequestBody JoinGameRequest request) {

        Game game = gameService.joinGame(request.getGameId(), request.getPlayerName());

        GameStateResponse response = gameMapper.toGameStateResponse(game, request.getPlayerName());

        response.setGameId(game.getId());

        return ResponseEntity.ok(response);
    }
}
