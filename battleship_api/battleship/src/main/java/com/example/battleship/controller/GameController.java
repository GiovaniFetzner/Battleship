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

    public GameController(GameService gameService,
                          GameMapper gameMapper) {
        this.gameService = gameService;
        this.gameMapper = gameMapper;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> create(
            @RequestBody CreateGameRequest request) {

        Game game = gameService.createGame(request.getPlayerName());

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, request.getPlayerName());

        response.setGameId(game.getId());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(
            @PathVariable String gameId,
            @RequestParam String playerName) {

        Game game = gameService.getGameState(gameId);

        // valida se pertence ao jogo
        game.findPlayer(playerName);

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, playerName);

        response.setGameId(gameId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateResponse> joinGame(
            @PathVariable String gameId,
            @RequestBody JoinGameRequest request) {

        Game game = gameService.joinGame(gameId, request.getPlayerName());

        GameStateResponse response =
                gameMapper.toGameStateResponse(game, request.getPlayerName());

        response.setGameId(game.getId());

        return ResponseEntity.ok(response);
    }

}
