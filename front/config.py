import pygame
pygame.init()

# --- Configurações principais ---
TAMANHO_CELULA = 50
LINHAS = 10
COLUNAS = 10
LARGURA_GRID = COLUNAS * TAMANHO_CELULA
ALTURA_GRID = LINHAS * TAMANHO_CELULA
LARGURA_SIDEBAR = 450
LARGURA_TELA = LARGURA_GRID + LARGURA_SIDEBAR
ALTURA_TELA = ALTURA_GRID

# --- Cores ---
COR_FUNDO = (20, 25, 35)
COR_GRID = (90, 100, 120)
COR_SIDEBAR = (35, 40, 55)
COR_TEXTO = (235, 235, 240)

# --- Fonte ---
pygame.font.init()
FONTE_TITULO = pygame.font.SysFont("arial", 26, bold=True)
FONTE_TEXTO = pygame.font.SysFont("arial", 20, bold=False)
