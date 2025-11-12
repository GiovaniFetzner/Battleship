import pygame
import os
from front.interface import TAMANHO_CELULA

class Barco:
    def __init__(self, nome, tamanho, cor, x=0, y=0, horizontal=True, imagem=None):
        self.nome = nome
        self.tamanho = tamanho
        self.cor = cor
        self.x = x
        self.y = y
        self.horizontal = horizontal
        self.imagem = imagem  # imagem carregada
        self.acertos = set()  # células atingidas
        self.destruido = False

    # ----------------------------
    # LÓGICA DE JOGO
    # ----------------------------
    def get_posicoes(self):
        """Retorna todas as células ocupadas pelo barco."""
        return [
            (self.x + i, self.y) if self.horizontal else (self.x, self.y + i)
            for i in range(self.tamanho)
        ]

    def registrar_atingido(self, x, y):
        """Marca uma célula como atingida, se fizer parte do barco."""
        if (x, y) in self.get_posicoes():
            self.acertos.add((x, y))

    def receber_tiro(self, x, y):
        """Registra um tiro e retorna o resultado."""
        if (x, y) in self.get_posicoes():
            self.acertos.add((x, y))
            if len(self.acertos) == self.tamanho:
                self.destruido = True
                return "destroyed"
            return "hit"
        return None

    # ----------------------------
    # DESENHO NO FRONT
    # ----------------------------
    def desenhar(self, tela):
        """Desenha o barco na tela usando sua imagem, se houver."""
        if self.imagem:
            if self.horizontal:
                px = self.x * TAMANHO_CELULA
                py = self.y * TAMANHO_CELULA
            else:
                px = self.x * TAMANHO_CELULA
                py = self.y * TAMANHO_CELULA
            tela.blit(self.imagem, (px, py))
        else:
            # fallback simples (sem imagem)
            for i in range(self.tamanho):
                if self.horizontal:
                    px = (self.x + i) * TAMANHO_CELULA
                    py = self.y * TAMANHO_CELULA
                    pos = (self.x + i, self.y)
                else:
                    px = self.x * TAMANHO_CELULA
                    py = (self.y + i) * TAMANHO_CELULA
                    pos = (self.x, self.y + i)

                rect = pygame.Rect(px, py, TAMANHO_CELULA, TAMANHO_CELULA)
                cor = (100, 100, 100) if pos in self.acertos else self.cor
                pygame.draw.rect(tela, cor, rect)
                pygame.draw.rect(tela, (200, 200, 200), rect, 1)


# ----------------------------
# FUNÇÃO AUXILIAR
# ----------------------------
HEADLESS = os.environ.get("HEADLESS", "false").lower() == "true"

def carregar_imagem(nome_arquivo, tamanho, horizontal=True):
    if HEADLESS:
        # cria uma superfície "vazia" para não falhar
        surf = pygame.Surface((tamanho * 20, 20), pygame.SRCALPHA)
        surf.fill((100, 100, 100, 255))
        return surf
    else:
        imagem = pygame.image.load(f"assets/{nome_arquivo}").convert_alpha()
        if not horizontal:
            imagem = pygame.transform.rotate(imagem, 90)
        return imagem
