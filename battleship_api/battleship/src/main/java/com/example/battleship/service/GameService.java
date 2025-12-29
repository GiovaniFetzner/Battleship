package com.example.battleship.service;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;

public interface GameService {

    /**
     * Cria um novo jogo e adiciona o primeiro jogador
     */
    GameStateResponse createGame(JoinGameRequest request);

    /**
     * Adiciona o segundo jogador a um jogo existente
     */
    GameStateResponse joinGame(String gameId, JoinGameRequest request);

    /**
     * Inicia o jogo (muda estado de WAITING para IN_PROGRESS)
     */
    GameStateResponse startGame(String gameId);

    /**
     * Posiciona um navio no tabuleiro do jogador
     */
    GameStateResponse placeShip(PlaceShipRequest request);

    /**
     * Realiza um ataque no tabuleiro do oponente
     */
    AttackResultResponse attack(AttackRequest request);

    /**
     * Obtém o estado atual do jogo
     */
    GameStateResponse getGameState(String gameId, String playerId);

    /**
     * Lista todos os jogos ativos
     */
    java.util.List<GameStateResponse> listActiveGames();

    /**
     * Remove um jogo (cleanup)
     */
    void deleteGame(String gameId);
}
