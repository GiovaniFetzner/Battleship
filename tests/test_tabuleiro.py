# OBS: Para rodar o teste precisa estar no diretório raiz do projeto: py -m tests.test_tabuleiro

import pygame
pygame.display.init()
pygame.display.set_mode((1, 1))  # necessário para carregar imagens com alpha

from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes

# Inicializa o tabuleiro e adiciona barcos manualmente
tab = Tabuleiro(10, 10)
lancha = Lancha(2, 3, horizontal=True)
submarino = Submarino(5, 1, horizontal=False)

tab.adicionar_barco(lancha)
tab.adicionar_barco(submarino)

print("=== Estado inicial ===")
for b in tab.barcos:
    print(f"{b.nome}: {b.get_posicoes()}")

# Simula alguns tiros
tiros = [(2, 3), (3, 3), (5, 1), (5, 2), (5, 3)]

for x, y in tiros:
    resultado = tab.receber_tiro(x, y)
    print(f"Tiro em ({x},{y}) -> {resultado}")

# Mostra status final dos barcos
print("\n=== Resultado final ===")
for b in tab.barcos:
    print(f"{b.nome} destruído? {b.destruido()} | Acertos: {b.acertos}")

print(f"\nJogador perdeu? {tab.todos_destruidos()}")
