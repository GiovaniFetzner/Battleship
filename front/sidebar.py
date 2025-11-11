import pygame
from front.config import LARGURA_GRID, LARGURA_SIDEBAR, ALTURA_TELA, COR_SIDEBAR, COR_TEXTO, FONTE_TITULO, FONTE_TEXTO, LARGURA_TELA

def desenhar_sidebar(tela):
    sidebar_rect = pygame.Rect(LARGURA_GRID, 0, LARGURA_SIDEBAR, ALTURA_TELA)
    pygame.draw.rect(tela, COR_SIDEBAR, sidebar_rect)

    # --- Título ---
    titulo = FONTE_TITULO.render("Painel do Jogo", True, COR_TEXTO)
    tela.blit(titulo, (LARGURA_GRID + 20, 20))

    pygame.draw.line(
        tela, (100, 100, 120), (LARGURA_GRID + 15, 60), (LARGURA_TELA - 15, 60), 1
    )

    # --- Dados fictícios ---
    jogadores = [
        {"nome": "Jogador 1", "tiros": 12, "acertos": 5},
        {"nome": "Jogador 2", "tiros": 10, "acertos": 3},
    ]

    y = 90
    for j in jogadores:
        nome = FONTE_TITULO.render(f"{j['nome']}", True, COR_TEXTO)
        tiros = FONTE_TEXTO.render(f"Tiros: {j['tiros']}", True, COR_TEXTO)
        acertos = FONTE_TEXTO.render(f"Acertos: {j['acertos']}", True, COR_TEXTO)

        tela.blit(nome, (LARGURA_GRID + 25, y))
        tela.blit(tiros, (LARGURA_GRID + 25, y + 35))
        tela.blit(acertos, (LARGURA_GRID + 25, y + 60))
        y += 110
