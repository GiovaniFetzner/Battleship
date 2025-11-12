import pygame
import random
import socket
import threading
from front.interface import Interface, LARGURA_TELA, ALTURA_TELA, COR_FUNDO
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes
from config_network import HOSTS, PLAYER_ID, UDP_PORT_SERVER

def main():
    pygame.init()

    # --- Tela ---
    tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
    pygame.display.set_caption(f"Batalha Naval - Jogador {PLAYER_ID}")
    clock = pygame.time.Clock()

    # --- Tabuleiro e barcos ---
    tabuleiro = Tabuleiro()
    tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
    tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

    # --- Interface ---
    interface = Interface()
    jogadores = [
        {"nome": f"Jogador {PLAYER_ID}", "acertos": 0},
        {"nome": "Adversários", "acertos": 0},
    ]
    interface.atualizar_jogadores(jogadores)

    # --- Configuração de rede ---
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', UDP_PORT_SERVER))
    print(f"[NET] Jogador {PLAYER_ID} escutando na porta {UDP_PORT_SERVER}")

    def ouvir_rede():
        while True:
            try:
                data, addr = sock.recvfrom(1024)
                msg = data.decode().strip()
                if msg.startswith("TIRO"):
                    _, x, y, autor = msg.split(',')
                    x, y, autor = int(x), int(y), int(autor)
                    if autor != PLAYER_ID:
                        resultado = tabuleiro.receber_tiro(x, y)
                        interface.adicionar_log(f"[REDE] Tiro de J{autor} em ({x},{y}) -> {resultado}")
                        if resultado in ("hit", "destroyed"):
                            jogadores[1]["acertos"] += 1
                        interface.atualizar_jogadores(jogadores)
            except Exception as e:
                print("[ERRO REDE]", e)

    threading.Thread(target=ouvir_rede, daemon=True).start()

    # --- Loop principal ---
    rodando = True
    tempo_tiro = 0
    intervalo = 2000  # ms entre tiros automáticos
    posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
    random.shuffle(posicoes_disponiveis)

    while rodando:
        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                rodando = False
            elif evento.type == pygame.MOUSEWHEEL:
                pos_mouse = pygame.mouse.get_pos()
                if interface.mouse_sobre_log(pos_mouse):
                    interface.mover_scroll(-evento.y)

        # --- Tiros automáticos ---
        tempo_tiro += clock.get_time()
        if tempo_tiro >= intervalo and posicoes_disponiveis:
            x, y = posicoes_disponiveis.pop()
            msg = f"TIRO,{x},{y},{PLAYER_ID}"
            for h in HOSTS:
                sock.sendto(msg.encode(), (h["ip"], h["porta"]))
            interface.adicionar_log(f"[ENVIO] Tiro ({x},{y}) enviado por J{PLAYER_ID}")
            tempo_tiro = 0

        # --- Verifica derrota ---
        if tabuleiro.todos_destruidos():
            interface.adicionar_log(f"[DERROTA] Jogador {PLAYER_ID} foi derrotado!")
            tela.fill(COR_FUNDO)
            tabuleiro.desenhar(tela)
            interface.desenhar_log(tela)
            interface.desenhar_hud(tela)
            pygame.display.flip()
            pygame.time.wait(3000)
            rodando = False

        # --- Desenho ---
        tela.fill(COR_FUNDO)
        tabuleiro.desenhar(tela)
        interface.desenhar_log(tela)
        interface.desenhar_hud(tela)
        pygame.display.flip()

        clock.tick(60)

    pygame.quit()


if __name__ == "__main__":
    main()