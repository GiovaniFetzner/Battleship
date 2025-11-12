import pygame
from front.interface import Interface, LARGURA_TELA, ALTURA_TELA, COR_FUNDO
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes
import random

def main():
    pygame.init()
    tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
    pygame.display.set_caption("Batalha Naval")
    relogio = pygame.time.Clock()

    # --- Tabuleiro ---
    tabuleiro = Tabuleiro()
    tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
    tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

    # --- Interface (HUD + log) ---
    interface = Interface()
    
    # Lista de jogadores fictícia para teste
    jogadores = [
        {"nome": "Você", "acertos": 0},
        {"nome": "Adversário", "acertos": 0},
    ]
    interface.atualizar_jogadores(jogadores)

    # --- Loop principal ---
    rodando = True
    tempo_tiro = 0
    intervalo = 1000  # ms entre tiros
    clock = pygame.time.Clock()

    # Lista de todas as posições do tabuleiro para simulação de tiros
    posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
    random.shuffle(posicoes_disponiveis)

    while rodando:
        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                rodando = False

            # --- Scroll do log com o mouse ---
            elif evento.type == pygame.MOUSEWHEEL:
                pos_mouse = pygame.mouse.get_pos()
                if interface.mouse_sobre_log(pos_mouse):
                    # pygame inverte o eixo Y do scroll (pra cima = y=1, pra baixo = y=-1)
                    interface.mover_scroll(-evento.y)

        # --- Simula tiros automáticos ---
        tempo_tiro += clock.get_time()
        if tempo_tiro >= intervalo and posicoes_disponiveis:
            x, y = posicoes_disponiveis.pop()
            resultado = tabuleiro.receber_tiro(x, y)
            interface.adicionar_log(f"Tiro em ({x},{y}) -> {resultado}")

            # Atualiza HUD fictício de acertos
            if resultado in ("hit", "destroyed"):
                jogadores[0]["acertos"] += 1
            interface.atualizar_jogadores(jogadores)
            tempo_tiro = 0
                    
        if tabuleiro.todos_destruidos():
            interface.adicionar_log("lost")

            # redesenha tudo uma última vez antes de pausar
            tela.fill(COR_FUNDO)
            tabuleiro.desenhar(tela)
            interface.desenhar_log(tela)
            interface.desenhar_hud(tela)
            pygame.display.flip()

            pygame.time.wait(3000)  # espera 3 segundos antes de fechar
            rodando = False

        # --- Desenho ---
        tela.fill(COR_FUNDO)
        tabuleiro.desenhar(tela)
        interface.desenhar_log(tela)
        interface.desenhar_hud(tela)

        pygame.display.flip()
        clock.tick(60)

    pygame.quit()

if __name__ == "__main__":
    main()
