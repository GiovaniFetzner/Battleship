# OBS: Para rodar o teste precisa estar no diretório raiz do projeto: py -m tests.test_tabuleiro

import pygame
import random
import time

# Inicializa o Pygame (necessário para carregar imagens com alpha)
pygame.display.init()
pygame.display.set_mode((1, 1))

from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes

# ------------------------------------------------------
# Configuração inicial do tabuleiro
# ------------------------------------------------------
tab = Tabuleiro(10, 10)
tipos_barcos = [Lancha, Submarino, Bombardeiro, PortaAvioes]
tab.posicionar_barcos_automaticamente(tipos_barcos)

print("=== Estado inicial dos barcos ===")
for b in tab.barcos:
    print(f"{b.nome}: {b.get_posicoes()}")

# ------------------------------------------------------
# Simulação de execução automática (modo local)
# ------------------------------------------------------
print("\n=== Iniciando simulação automática de tiros ===")

turno = 1
# Define todas as posições possíveis (0..9, 0..9)
todas_posicoes = [(x, y) for x in range(tab.colunas) for y in range(tab.linhas)]
random.shuffle(todas_posicoes)

while not tab.todos_destruidos() and todas_posicoes:
    print(f"\n--- Turno {turno} ---")
    # Escolhe uma posição aleatória para atirar
    x, y = todas_posicoes.pop()
    resultado = tab.receber_tiro(x, y)

    if resultado != "miss":
        print(f"💥 Tiro em ({x},{y}) -> {resultado.upper()}")
    else:
        print(f"Tiro em ({x},{y}) -> {resultado}")

    # Mostra estado atual de cada barco
    for b in tab.barcos:
        print(f"  {b.nome:<15} destruído={b.destruido} | acertos={b.acertos}")

    turno += 1
    time.sleep(1)  # pausa de 1s só para visualização (poderia ser 10s)

print("\n=== Fim da simulação ===")
tab.exibir_estado()
print(f"Jogador perdeu? {tab.todos_destruidos()}")