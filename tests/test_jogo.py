import pygame
import random
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes
from front.config import TAMANHO_CELULA, LINHAS, COLUNAS

pygame.init()
LARGURA, ALTURA = 600, 600
tela = pygame.display.set_mode((LARGURA, ALTURA))
pygame.display.set_caption("Simulação de Batalha Naval")

# --- Cria o tabuleiro e posiciona automaticamente os barcos ---
tab = Tabuleiro(LINHAS, COLUNAS)
tipos = [Lancha, Submarino, Bombardeiro, PortaAvioes]
tab.posicionar_barcos_automaticamente(tipos)

# --- Inicializa lista de posições disponíveis (para tiros aleatórios) ---
posicoes_disponiveis = [(x, y) for x in range(LINHAS) for y in range(COLUNAS)]
random.shuffle(posicoes_disponiveis)

clock = pygame.time.Clock()
rodando = True
tempo_tiro = 0
intervalo = 500  # 0.5 segundos entre tiros para simular rápido

while rodando:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            rodando = False

    tempo_tiro += clock.get_time()
    if tempo_tiro >= intervalo and posicoes_disponiveis:
        x, y = posicoes_disponiveis.pop()  # Remove para não repetir
        resultado = tab.receber_tiro(x, y)
        print(f"shot:{x},{y} -> {resultado}")

        # Verifica se todos os barcos foram destruídos
        if tab.todos_destruidos():
            print("lost")  # <-- Aqui você enviaria via UDP
            rodando = False

        tempo_tiro = 0

    # Atualiza a tela
    tela.fill((20, 20, 20))
    tab.desenhar(tela)
    pygame.display.flip()
    clock.tick(30)

pygame.quit()
