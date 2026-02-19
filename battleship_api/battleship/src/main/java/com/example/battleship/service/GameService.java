package com.example.battleship.service;

import com.example.battleship.domain.game.Game;
import com.example.battleship.domain.map.AttackResult;

import java.util.List;

public interface GameService {

    /**
     * Cria um novo jogo e adiciona o primeiro jogador
     * 
     * @param playerName Nome do jogador que está a criar o jogo
     *                   
     * @return O estado inicial do jogo criado, incluindo o jogador que criou o jogo e
     */
    Game createGame(String playerName);


    /**
     * Posiciona um navio no tabuleiro do jogador
     * 
     * @param gameId ID do jogo
     * @param playerName ID do jogador que está posicionando o navio
     * @param shipType Tipo do navio (e.g., "Destroyer", "
     *                 Submarine", "Cruiser", "Battleship", "Carrier")
     * @param size Tamanho do navio (número de células que ocupa)
     * @param x Coordenada X do ponto inicial do navio
     * @param y Coordenada Y do ponto inicial do navio
     * @param orientation Orientação do navio ("HORIZONTAL" ou "VERTICAL")
     *                    
     * @return O estado atualizado do jogo após posicionar o navio
     */
    Game placeShip(String gameId,
                                String playerName,
                                String shipType,
                                int size,
                                int x,
                                int y,
                                String orientation);

    /**
     * Realiza um ataque no tabuleiro do oponente
     * 
     * @param gameId ID do jogo
     * @param playerId ID do jogador que está atacando
     * @param x Coordenada X do ataque
     * @param y Coordenada Y do ataque
     *          
     * @return O resultado do ataque, indicando se foi um acerto, erro ou afundamento, e o estado atualizado do jogo
     */
    AttackResult attack(String gameId,
                        String playerId,
                        int x,
                        int y);

    /**
     * Obtém o estado atual do jogo
     * 
     * @param gameId ID do jogo
     * @return O estado atual do jogo, incluindo informações sobre os jogadores, tabuleiros, navios e o estado do jogo (WAITING, IN_PROGRESS, FINISHED)
     */
    Game getGameState(String gameId);

    /**
     * Lista todos os jogos ativos (em andamento).
     * 
     * @return Lista de jogos que estão atualmente em andamento (estado IN_PROGRESS)
     */
    List<Game> listActiveGames();

    /**
     * Deleta um jogo existente usando o código da sala. Isso pode ser usado para limpar jogos antigos ou permitir que os jogadores saiam de um jogo antes de começar.
     * 
     * @param gameId ID do jogo a ser deletado
     */
    void deleteGame(String gameId);

    /**
     * Adiciona um jogador a um jogo existente usando o código da sala
     * 
     * @param gameId ID do jogo
     * @param playerName Nome do jogador que deseja entrar
     * @return O estado atualizado do jogo com o novo jogador adicionado
     */
    Game joinGame(String gameId, String playerName);

    /**
     * Marca um jogador como pronto para iniciar o jogo. O jogo só pode começar quando ambos os jogadores estiverem prontos.
     * @param gameId ID do jogo
     * @param playerId ID do jogador
     * @return true se ambos os jogadores estiverem prontos e o jogo pode ser iniciado, false caso contrário
     */
    boolean confirmPlayerReady(String gameId, String playerId);
    
    /**
     * Obtém o nome do jogador que tem a vez atual de jogar
     *
     * @param gameId ID do jogo
     *
     * @return O nome do jogador que tem a vez atual de jogar
     */

    String getCurrentPlayer(String gameId);

    /**
     * Verifica se o jogo terminou (quando um jogador afunda todos os navios do oponente)
     *
     * @param gameId ID do jogo
     *
     * @return true se o jogo terminou, false caso contrário
     */
    boolean isGameOver(String gameId);

    /**
     * Obtém o nome do jogador vencedor, se o jogo terminou
     *
     * @param gameId ID do jogo
     *
     * @return O nome do jogador vencedor se o jogo terminou, ou null se o jogo ainda está em andamento
     */
    String getWinner(String gameId);

}
