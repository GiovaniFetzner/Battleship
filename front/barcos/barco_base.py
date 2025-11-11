import pygame
from front.config import TAMANHO_CELULA

class Barco:
    def __init__(self, nome, tamanho, cor, x=0, y=0, horizontal=True):
        self.nome = nome
        self.tamanho = tamanho
        self.cor = cor
        self.x = x
        self.y = y
        self.horizontal = horizontal
        self.imagem = None

    def desenhar(self, tela):
        if self.imagem:
            tela.blit(
                self.imagem,
                (
                    self.x * TAMANHO_CELULA + getattr(self, "offset_x", 0),
                    self.y * TAMANHO_CELULA + getattr(self, "offset_y", 0),
                ),
            )
        else:
            for i in range(self.tamanho):
                px = (self.x + i) * TAMANHO_CELULA if self.horizontal else self.x * TAMANHO_CELULA
                py = self.y * TAMANHO_CELULA if self.horizontal else (self.y + i) * TAMANHO_CELULA
                rect = pygame.Rect(px, py, TAMANHO_CELULA, TAMANHO_CELULA)
                pygame.draw.rect(tela, self.cor, rect)
                pygame.draw.rect(tela, (200, 200, 200), rect, 1)

    def get_posicoes(self):
        return [
            (self.x + i, self.y) if self.horizontal else (self.x, self.y + i)
            for i in range(self.tamanho)
        ]

def carregar_imagem(nome_arquivo, tamanho_celulas, horizontal=True):
    """Carrega e ajusta uma imagem de barco conforme o tamanho e a orientação."""
    imagem = pygame.image.load(f"assets/{nome_arquivo}").convert_alpha()
    if horizontal:
        largura = TAMANHO_CELULA * tamanho_celulas
        altura = TAMANHO_CELULA
        imagem = pygame.transform.scale(imagem, (largura, altura))
        offset_x, offset_y = 0, 0
    else:
        largura = TAMANHO_CELULA
        altura = TAMANHO_CELULA * tamanho_celulas
        imagem = pygame.transform.scale(imagem, (altura, largura))
        imagem = pygame.transform.rotate(imagem, 90)
        offset_x, offset_y = 0, 0
    return imagem, offset_x, offset_y
