import pygame
import random
import socket
import threading
import queue
import time

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
# Criamos a lista de jogadores a partir do HOSTS inicial.
# Como HOSTS pode mudar dinamicamente, a lista será expandida quando necessário.
jogadores = [{"nome": f"Jogador {i+1}", "acertos": 0} for i in range(len(HOSTS))]
interface = Interface()
interface.atualizar_jogadores(jogadores)

# --- Fila para eventos vindos da rede (para serem consumidos no loop principal) ---
fila_rede = queue.Queue()

# --- Configuração UDP ---
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
# Escuta em todas as interfaces para garantir que 127.0.0.1 <-> 127.0.0.2 funcione
sock.bind(('', UDP_PORT))
print(f"[NET] Jogador {PLAYER_ID} escutando na porta {UDP_PORT}")

# --- Função para ouvir tiros de outros jogadores (thread) ---
def ouvir_rede():
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            # aceitamos mensagens no formato: TIRO,x,y,autor
            # também aceitamos shot,x,y,autor por compatibilidade
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

                    # coloca evento na fila para o loop principal processar
                    fila_rede.put({"tipo": "tiro", "x": x, "y": y, "autor": autor, "origem": addr[0]})
                else:
                    print("[REDE] Mensagem TIRO com formato incorreto:", msg)
            else:
                # outros tipos de mensagem podem ser tratados aqui (ex.: sincronização, participantes)
                pass
        except Exception as e:
            print("[ERRO REDE]", e)
            # pequena espera para evitar loop apertado em caso de erro contínuo
            time.sleep(0.1)

# inicia thread de escuta
threading.Thread(target=ouvir_rede, daemon=True).start()

# --- Função para disparar tiro ---
def enviar_tiro(x, y, lista_adversarios):
    # protocolo padronizado: TIRO,x,y,autor
    msg = f"TIRO,{x},{y},{PLAYER_ID}"
    enviado = False
    for h in lista_adversarios:
        try:
            sock.sendto(msg.encode(), (h["ip"], h["porta"]))
            enviado = True
        except Exception as e:
            print(f"[ERRO ENVIO] não foi possível enviar para {h}: {e}")
    if enviado:
        interface.adicionar_log(f"[ENVIO] TIRO ({x},{y}) enviado por J{PLAYER_ID}")
    else:
        interface.adicionar_log(f"[ENVIO] Falha ao enviar TIRO ({x},{y}) por J{PLAYER_ID}")

# --- Lista de posições disponíveis (aleatória, sem repetir) ---
posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
random.shuffle(posicoes_disponiveis)
intervalo_tiro = 2000  # ms entre tiros automáticos
tempo_tiro = 0

# --- Loop principal ---
rodando = True
while rodando:
    dt_ms = clock.tick(60)  # tempo em ms desde o último frame
    # --- Consome eventos da fila de rede (FEITO NA THREAD PRINCIPAL) ---
    try:
        # esvazia todos os eventos disponíveis no momento
        while True:
            evento_rede = fila_rede.get_nowait()
            if evento_rede["tipo"] == "tiro":
                x = evento_rede["x"]
                y = evento_rede["y"]
                autor = evento_rede["autor"]

                # garante que a lista 'jogadores' tem tamanho suficiente
                if autor - 1 >= len(jogadores):
                    # expande jogadores até index existir
                    for idx in range(len(jogadores), autor):
                        jogadores.append({"nome": f"Jogador {idx+1}", "acertos": 0})

                # aplica o tiro no tabuleiro local (quem recebeu foi atingido)
                resultado = tabuleiro.receber_tiro(x, y)
                interface.adicionar_log(f"[REDE] Tiro de J{autor} em ({x},{y}) -> {resultado}")

                # se o tiro foi acerto, incrementa acerto do autor (se autor não for eu)
                if resultado in ("hit", "destroyed"):
                    # cuidado: autor pode ser eu (por eco). só conta se não for eu.
                    if autor != PLAYER_ID:
                        jogadores[autor - 1]["acertos"] += 1

                # atualiza HUD
                interface.atualizar_jogadores(jogadores)
            fila_rede.task_done()
    except queue.Empty:
        pass

    # --- Events do pygame ---
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
                interface.adicionar_log(f"[ERRO] ao tentar copiar: {e}")

    # Recalcula adversários a cada frame (HOSTS pode ser dinâmico)
    adversarios = [h for h in HOSTS if h["ip"] != LOCAL_IP]

    # Se houver mais hosts do que a lista 'jogadores', expande a lista de jogadores
    if len(HOSTS) > len(jogadores):
        for idx in range(len(jogadores), len(HOSTS)):
            jogadores.append({"nome": f"Jogador {idx+1}", "acertos": 0})
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
