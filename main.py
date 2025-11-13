import os
import random
import socket
import threading
import queue
import time

# --- Configurações de modo ---
MODE = os.getenv("MODE", "server-gui")  # server-gui ou client-headless
PORT_UDP = int(os.getenv("PORT_UDP", 5000))
PORT_TCP = int(os.getenv("PORT_TCP", 5001))
CONNECT_TO = os.getenv("CONNECT_TO", None)  # usado apenas em client-headless

# --- Importa Pygame apenas se for modo GUI ---
if MODE == "server-gui":
    import pygame
    from front.interface import Interface, LARGURA_TELA, ALTURA_TELA, COR_FUNDO
    from front.tabuleiro import Tabuleiro
    from front.barcos.lancha import Lancha
    from front.barcos.submarino import Submarino
    from front.barcos.bombardeiro import Bombardeiro
    from front.barcos.porta_avioes import PortaAvioes

# --- Inicialização da rede ---
from config_network import init_network, HOSTS, PLAYER_ID

# Bind do servidor
LOCAL_IP = "0.0.0.0" if MODE == "server-gui" else None
init_network()

# --- Inicialização GUI ---
if MODE == "server-gui":
    pygame.init()
    tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
    pygame.display.set_caption(f"Batalha Naval - Jogador {PLAYER_ID}")
    clock = pygame.time.Clock()

# --- Criação do tabuleiro e barcos ---
if MODE == "server-gui":
    tabuleiro = Tabuleiro()
    tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
    tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

# --- Interface ---
if MODE == "server-gui":
    jogadores = [{"nome": f"Jogador {i+1}", "acertos": 0} for i in range(len(HOSTS))]
    interface = Interface()
    interface.atualizar_jogadores(jogadores)
else:
    interface = None  # modo headless

# --- Fila para eventos da rede ---
fila_rede = queue.Queue()

# --- Configuração UDP ---
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
if MODE == "server-gui":
    sock.bind(('', PORT_UDP))
    print(f"[NET] Jogador {PLAYER_ID} escutando na porta {PORT_UDP}")

# --- Thread para ouvir a rede ---
def ouvir_rede():
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            if msg.startswith("TIRO") or msg.startswith("shot") or msg.startswith("SHOT"):
                parts = msg.split(',')
                if len(parts) >= 4:
                    tipo = parts[0].upper()
                    try:
                        x = int(parts[1])
                        y = int(parts[2])
                        autor = int(parts[3])
                    except ValueError:
                        print("[REDE] Mensagem TIRO inválida (valores):", msg)
                        continue
                    fila_rede.put({"tipo": "tiro", "x": x, "y": y, "autor": autor, "origem": addr[0]})
                else:
                    print("[REDE] Mensagem TIRO com formato incorreto:", msg)
            else:
                pass  # outros tipos de mensagem
        except Exception as e:
            print("[ERRO REDE]", e)
            time.sleep(0.1)

threading.Thread(target=ouvir_rede, daemon=True).start()

# --- Função para disparar tiros ---
def enviar_tiro(x, y, lista_adversarios):
    msg = f"TIRO,{x},{y},{PLAYER_ID}"
    enviado = False
    for h in lista_adversarios:
        try:
            sock.sendto(msg.encode(), (h["ip"], h["porta"]))
            enviado = True
        except Exception as e:
            print(f"[ERRO ENVIO] não foi possível enviar para {h}: {e}")
    if interface:
        if enviado:
            interface.adicionar_log(f"[ENVIO] TIRO ({x},{y}) enviado por J{PLAYER_ID}")
        else:
            interface.adicionar_log(f"[ENVIO] Falha ao enviar TIRO ({x},{y}) por J{PLAYER_ID}")

# --- Loop principal ---
posicoes_disponiveis = []
if MODE == "server-gui":
    posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
    random.shuffle(posicoes_disponiveis)
intervalo_tiro = 2000  # ms
tempo_tiro = 0

if MODE == "server-gui":
    rodando = True
    while rodando:
        dt_ms = clock.tick(60)
        # --- Consome eventos da fila de rede ---
        try:
            while True:
                evento_rede = fila_rede.get_nowait()
                if evento_rede["tipo"] == "tiro":
                    x = evento_rede["x"]
                    y = evento_rede["y"]
                    autor = evento_rede["autor"]
                    if autor - 1 >= len(jogadores):
                        for idx in range(len(jogadores), autor):
                            jogadores.append({"nome": f"Jogador {idx+1}", "acertos": 0})
                    resultado = tabuleiro.receber_tiro(x, y)
                    interface.adicionar_log(f"[REDE] Tiro de J{autor} em ({x},{y}) -> {resultado}")
                    if resultado in ("hit", "destroyed") and autor != PLAYER_ID:
                        jogadores[autor - 1]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)
                    fila_rede.task_done()
        except queue.Empty:
            pass

        # --- Eventos Pygame ---
        for evento in pygame.event.get():
            if evento.type == pygame.QUIT:
                rodando = False
            elif evento.type == pygame.MOUSEWHEEL:
                pos_mouse = pygame.mouse.get_pos()
                if interface.mouse_sobre_log(pos_mouse):
                    interface.mover_scroll(-evento.y)
            elif evento.type == pygame.KEYDOWN:
                if evento.key == pygame.K_c and (evento.mod & pygame.KMOD_CTRL):
                    sucesso = interface.copiar_log(apenas_visivel=False)
                    if sucesso:
                        interface.adicionar_log("[INFO] Log copiado para a área de transferência (Ctrl+C)")
                    else:
                        interface.adicionar_log("[ERRO] Não foi possível copiar o log")

        # --- Recalcula adversários ---
        adversarios = [h for h in HOSTS if h["ip"] != LOCAL_IP]
        if len(HOSTS) > len(jogadores):
            for idx in range(len(jogadores), len(HOSTS)):
                jogadores.append({"nome": f"Jogador{idx+1}", "acertos": 0})
            interface.atualizar_jogadores(jogadores)

        # --- Tiros automáticos ---
        tempo_tiro += dt_ms
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

    pygame.quit()

else:
    # --- Modo headless (container) ---
    print("[INFO] Rodando modo headless (container)")
    # simula loop automático de disparo
    while True:
        adversarios = [h for h in HOSTS if h["ip"] != LOCAL_IP]
        if adversarios:
            x = random.randint(0, 9)
            y = random.randint(0, 9)
            enviar_tiro(x, y, adversarios)
        time.sleep(2)
