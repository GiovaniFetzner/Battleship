import pygame
from front.config import TAMANHO_CELULA, LINHAS, COLUNAS, LARGURA_GRID, ALTURA_GRID, COR_GRID

def desenhar_grid(tela):
    for i in range(LINHAS + 1):
        pygame.draw.line(
            tela, COR_GRID, (0, i * TAMANHO_CELULA), (LARGURA_GRID, i * TAMANHO_CELULA), 1
        )
    for j in range(COLUNAS + 1):
        pygame.draw.line(
            tela, COR_GRID, (j * TAMANHO_CELULA, 0), (j * TAMANHO_CELULA, ALTURA_GRID), 1
        )
