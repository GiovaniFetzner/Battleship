import pygame
import random
from front.config import TAMANHO_CELULA, LINHAS, COLUNAS, LARGURA_GRID, ALTURA_GRID, COR_GRID
from front.barcos.barco_base import Barco

class Tabuleiro:
    def __init__(self, linhas=LINHAS, colunas=COLUNAS):
        self.linhas = linhas
        self.colunas = colunas
        self.barcos = []
        self.tiros_acertados = set()  
        self.tiros_errados = set()    
        
        # --- POSICIONAMENTO ---
    def posicao_valida(self, barco: Barco):
        for (x, y) in barco.get_posicoes():
            if x < 0 or y < 0 or x >= self.colunas or y >= self.linhas:
                return False
            for b in self.barcos:
                if (x, y) in b.get_posicoes():
                    return False
        return True

    def adicionar_barco(self, barco: Barco):
        if self.posicao_valida(barco):
            self.barcos.append(barco)
            return True
        return False

    def posicionar_barcos_automaticamente(self, tipos_barcos):
        for tipo_barco in tipos_barcos:
            colocado = False
            tentativas = 0

            while not colocado and tentativas < 200:
                horizontal = random.choice([True, False])

                if horizontal:
                    x = random.randint(0, self.colunas - tipo_barco().tamanho)
                    y = random.randint(0, self.linhas - 1)
                else:
                    x = random.randint(0, self.colunas - 1)
                    y = random.randint(0, self.linhas - tipo_barco().tamanho)

                barco = tipo_barco(x, y, horizontal)
                if self.adicionar_barco(barco):
                    colocado = True
                else:
                    tentativas += 1

            if not colocado:
                print(f" Não foi possível posicionar '{tipo_barco().nome}' após {tentativas} tentativas.")

    # --- LÓGICA DE ATAQUE ---
    def receber_tiro(self, x, y):
        for barco in self.barcos:
            resultado = barco.receber_tiro(x, y)
            if resultado in ("hit", "destroyed"):
                self.tiros_acertados.add((x, y))
                return resultado
        self.tiros_errados.add((x, y))
        return "miss"

    
    def todos_destruidos(self):
        return all(barco.destruido for barco in self.barcos)

    def atirar_em_aleatorio(self):
        todas_posicoes = [(x, y) for x in range(self.colunas) for y in range(self.linhas)]
        posicoes_disponiveis = [
            p for p in todas_posicoes 
            if p not in self.tiros_acertados and p not in self.tiros_errados
        ]

        if not posicoes_disponiveis:
            return None, None, "finished"

        x, y = random.choice(posicoes_disponiveis)
        resultado = self.receber_tiro(x, y)
        return x, y, resultado

    # --- DESENHO ---
    def desenhar(self, tela):
        self._desenhar_grid(tela)
        self._desenhar_barcos(tela)
        self._desenhar_tiros(tela)

    def _desenhar_grid(self, tela):
        for i in range(self.linhas + 1):
            pygame.draw.line(
                tela, COR_GRID, (0, i * TAMANHO_CELULA), (LARGURA_GRID, i * TAMANHO_CELULA), 1
            )
        for j in range(self.colunas + 1):
            pygame.draw.line(
                tela, COR_GRID, (j * TAMANHO_CELULA, 0), (j * TAMANHO_CELULA, ALTURA_GRID), 1
            )

    def _desenhar_barcos(self, tela):
        for barco in self.barcos:
            barco.desenhar(tela)

    def _desenhar_tiros(self, tela):
        # Desenha acertos (vermelho)
        for (x, y) in self.tiros_acertados:
            pygame.draw.circle(
                tela,
                (255, 0, 0),
                (x * TAMANHO_CELULA + TAMANHO_CELULA // 2, y * TAMANHO_CELULA + TAMANHO_CELULA // 2),
                TAMANHO_CELULA // 6,
            )

        # Desenha erros (azul claro)
        for (x, y) in self.tiros_errados:
            pygame.draw.circle(
                tela,
                (100, 150, 255),
                (x * TAMANHO_CELULA + TAMANHO_CELULA // 2, y * TAMANHO_CELULA + TAMANHO_CELULA // 2),
                TAMANHO_CELULA // 8,
            )