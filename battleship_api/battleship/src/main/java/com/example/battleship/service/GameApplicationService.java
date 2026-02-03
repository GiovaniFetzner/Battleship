package com.example.battleship.service;

import com.example.battleship.dto.inbound.AttackRequest;
import com.example.battleship.dto.inbound.JoinGameBaseRequest;
import com.example.battleship.dto.inbound.PlaceShipRequest;
import com.example.battleship.dto.outbound.AttackResultResponse;
import com.example.battleship.dto.outbound.GameStateResponse;

import java.util.List;

public interface GameApplicationService {

    /**
     * Cria um novo jogo e adiciona o primeiro jogador
     */
    GameStateResponse createGame(JoinGameBaseRequest request);

    /**
     * Adiciona o segundo jogador a um jogo existente
     */
    GameStateResponse joinGame(String gameId, JoinGameBaseRequest request);

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
    List<GameStateResponse> listActiveGames();

    /**
     * Remove um jogo (cleanup)
     */
    void deleteGame(String gameId);
}
