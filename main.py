import pygame
from front.config import *
from front.grid import desenhar_grid
from front.sidebar import desenhar_sidebar
from front.tabuleiro import Tabuleiro

# Importa barcos
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes

# Importa gerenciador de conexão
from network.conexao import GerenciadorConexao

def main():
    pygame.init()
    tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
    pygame.display.set_caption("Batalha Naval")
    relogio = pygame.time.Clock()

    # --- Inicializa rede ---
    conexao = GerenciadorConexao()
    conexao.iniciar()

    tabuleiro = Tabuleiro(LINHAS, COLUNAS)
    tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha, Lancha]
    tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

    # --- Loop principal ---
    rodando = True
    while rodando:
        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                rodando = False

        tela.fill(COR_FUNDO)
        desenhar_grid(tela)
        tabuleiro.desenhar(tela)
        desenhar_sidebar(tela)

        pygame.display.flip()
        relogio.tick(60)

    pygame.quit()

if __name__ == "__main__":
    main()
