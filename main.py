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
from config_network import HOSTS, PLAYER_ID, UDP_PORT, LOCAL_IP

# --- Inicialização do Pygame ---
pygame.init()
tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
pygame.display.set_caption(f"Batalha Naval - Jogador {PLAYER_ID}")
clock = pygame.time.Clock()

# --- Criação do tabuleiro e barcos ---
tabuleiro = Tabuleiro()
tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

# --- Interface ---
jogadores = [{"nome": f"Jogador {i+1}", "acertos": 0} for i in range(len(HOSTS))]
interface = Interface()
interface.atualizar_jogadores(jogadores)

# --- Configuração UDP ---
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(('', UDP_PORT))
print(f"[NET] Jogador {PLAYER_ID} escutando na porta {UDP_PORT}")

# --- Função para ouvir tiros de outros jogadores ---
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
                        jogadores[autor - 1]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)
        except Exception as e:
            print("[ERRO REDE]", e)

threading.Thread(target=ouvir_rede, daemon=True).start()

# --- Função para disparar tiro ---
def enviar_tiro(x, y, lista_adversarios):
    msg = f"shot,{x},{y},{PLAYER_ID}"
    for h in lista_adversarios:
        sock.sendto(msg.encode(), (h["ip"], h["porta"]))
    interface.adicionar_log(f"[ENVIO] shot ({x},{y}) enviado por J{PLAYER_ID}")


# --- Lista de posições disponíveis (aleatória, sem repetir) ---
posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
random.shuffle(posicoes_disponiveis)
intervalo_tiro = 2000  # ms entre tiros automáticos
tempo_tiro = 0

# --- Loop principal ---
rodando = True
while rodando:
    for evento in pygame.event.get():
        if evento.type == pygame.QUIT:
            rodando = False
        elif evento.type == pygame.MOUSEWHEEL:
            pos_mouse = pygame.mouse.get_pos()
            if interface.mouse_sobre_log(pos_mouse):
                interface.mover_scroll(-evento.y)
        elif evento.type == pygame.KEYDOWN:
            # Ctrl+C para copiar o log completo
            try:
                if evento.key == pygame.K_c and (evento.mod & pygame.KMOD_CTRL):
                    sucesso = interface.copiar_log(apenas_visivel=False)
                    if sucesso:
                        interface.adicionar_log("[INFO] Log copiado para a área de transferência (Ctrl+C)")
                    else:
                        interface.adicionar_log("[ERRO] Não foi possível copiar o log para a área de transferência")
            except Exception as e:
                # garantir que erros não quebrem o loop
                interface.adicionar_log(f"[ERRO] ao tentar copiar: {e}")

    adversarios = [h for h in HOSTS if h["ip"] != LOCAL_IP]

    # --- Tiros automáticos ---
    tempo_tiro += clock.get_time()
    if tempo_tiro >= intervalo_tiro and posicoes_disponiveis and adversarios:
        x, y = posicoes_disponiveis.pop()
        enviar_tiro(x, y, adversarios)
        tempo_tiro = 0

    # --- Desenho ---
    tela.fill(COR_FUNDO)
    tabuleiro.desenhar(tela)
    interface.desenhar_log(tela)
    interface.desenhar_hud(tela)
    pygame.display.flip()

    # --- Verifica derrota ---
    if tabuleiro.todos_destruidos():
        interface.adicionar_log(f"[DERROTA] Jogador {PLAYER_ID} foi derrotado!")
        pygame.time.wait(3000)
        rodando = False

    clock.tick(60)

pygame.quit()
