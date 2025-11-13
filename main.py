import pygame
import random
import threading
import queue
import time
import signal
import sys

from front.interface import Interface, LARGURA_TELA, ALTURA_TELA, COR_FUNDO
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes
from config_network import (
    HOSTS, PLAYER_ID, OPPONENT_IP, IS_DOCKER,
    enviar_tiro_para_todos, enviar_resposta_tcp, Jogador,
    enviar_saida, enviar_derrota
)

# --- Inicialização do Pygame ---
pygame.init()
tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
pygame.display.set_caption(f"Batalha Naval - Jogador {PLAYER_ID}")
clock = pygame.time.Clock()

# --- Criação do tabuleiro e barcos ---
tabuleiro = Tabuleiro()
tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

print("[DEBUG] Posicionamento local dos barcos:")
for b in tabuleiro.barcos:
    print(f"  {b.nome}: {b.get_posicoes()}")

# --- Interface ---
jogadores = [
    {"nome": f"Jogador {PLAYER_ID} (Você)", "acertos": 0},
    {"nome": f"Adversário ({OPPONENT_IP})", "acertos": 0}
]
interface = Interface()
interface.atualizar_jogadores(jogadores)

# --- Fila de rede ---
fila_rede = queue.Queue()
jugador_rede = Jogador()

# --- Estatísticas ---
meus_tiros_enviados = {}
OPPONENT_ID = 2 if PLAYER_ID == 1 else 1
active_opponents = {OPPONENT_ID: True}
vezes_atingido = 0
hits_by_player = {}

posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
random.shuffle(posicoes_disponiveis)
intervalo_tiro_min = 1500
intervalo_tiro_max = 3000
tempo_tiro = 0
rodando = True
necessita_redesenho = True

# --- Botão de saída ---
LARGURA_BOTAO = 200
ALTURA_BOTAO = 50
botao_sair_rect = pygame.Rect(LARGURA_TELA - LARGURA_BOTAO - 120, ALTURA_TELA - 80, LARGURA_BOTAO, ALTURA_BOTAO)
COR_BOTAO = (180, 50, 50)
COR_BOTAO_HOVER = (220, 70, 70)
COR_TEXTO_BOTAO = (255, 255, 255)
FONTE_BOTAO = pygame.font.SysFont("arial", 22, bold=True)

# --- Signal handler Ctrl+C ---
def handle_ctrl_c(signum, frame):
    global rodando
    print("\n[CTRL+C] Encerrando jogo...")
    interface.adicionar_log("[CTRL+C] Você pressionou Ctrl+C - encerrando")
    try:
        enviar_saida()
        print("[CTRL+C] Mensagem SAINDO enviada aos adversários")
    except Exception as e:
        print(f"[CTRL+C] Erro ao enviar SAINDO: {e}")
    rodando = False

signal.signal(signal.SIGINT, handle_ctrl_c)

# --- Funções de rede ---
def tratar_mensagem(msg):
    parts = msg.split(',')
    if not parts:
        return
    cmd = parts[0].strip().upper()

    if cmd == "TIRO" and len(parts) >= 4:
        try:
            x = int(parts[1])
            y = int(parts[2])
            autor = int(parts[3])
            fila_rede.put({"tipo": "tiro", "x": x, "y": y, "autor": autor})
        except ValueError:
            print("[REDE] Mensagem TIRO inválida:", msg)

    elif cmd == "SAINDO":
        leaving_id = int(parts[1]) if len(parts) >= 2 else 0
        active_opponents[leaving_id] = False
        interface.adicionar_log(f"[NET] Jogador {leaving_id} saiu — não enviará mais mensagens para ele")

    elif msg.strip().upper().startswith("LOST"):
        interface.adicionar_log(f"[NET] Adversário declarou DERROTA - você venceu!")
        active_opponents[OPPONENT_ID] = False

def tratar_resposta_tcp(msg):
    global meus_tiros_enviados, hits_by_player
    try:
        parts = msg.split(':')[1].split(',')
        resultado, x, y, autor = parts[0], int(parts[1]), int(parts[2]), int(parts[3])
        interface.adicionar_log(f"[TCP-RECEBIDO] {resultado.upper()} em ({x},{y}) de J{autor}")

        if resultado.lower() in ("hit", "destroyed"):
            pos = (x, y)
            if pos in meus_tiros_enviados:
                entry = meus_tiros_enviados[pos]
                if entry.get("status") == "pendente":
                    jogadores[0]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)
                    entry["status"] = "acertou"
                    opp = entry.get("opponent")
                    if opp is not None:
                        hits_by_player.setdefault(opp, 0)
                        hits_by_player[opp] += 1

        elif resultado.lower() == "destroyed":
            pos = (x, y)
            if pos in meus_tiros_enviados:
                entry = meus_tiros_enviados[pos]
                entry["status"] = "acertou"
                jogadores[0]["acertos"] += 1
                interface.atualizar_jogadores(jogadores)
                opp = entry.get("opponent")
                if opp is not None:
                    hits_by_player.setdefault(opp, 0)
                    hits_by_player[opp] += 1
    except Exception as e:
        print("[ERRO CALLBACK TCP]", e, msg)


jugador_rede.start_network(on_tiro=tratar_mensagem, on_res_tcp=tratar_resposta_tcp)

def enviar_tiro(x, y):
    msg = f"TIRO,{x},{y},{PLAYER_ID}"
    enviar_tiro_para_todos(msg)
    interface.adicionar_log(f"[ENVIO] TIRO ({x},{y}) enviado por J{PLAYER_ID}")

# ==============================
# Loop principal
# ==============================
while rodando:
    eventos_de_jogo = False

    # Processa fila de rede
    try:
        while True:
            evento_rede = fila_rede.get_nowait()
            eventos_de_jogo = True
            if evento_rede["tipo"] == "tiro":
                x, y, autor = evento_rede["x"], evento_rede["y"], evento_rede["autor"]
                resultado = tabuleiro.receber_tiro(x, y)
                interface.adicionar_log(f"[REDE] Tiro de J{autor} em ({x},{y}) -> {resultado}")

                if resultado in ("hit", "destroyed"):
                    jogadores[1]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)
                    vezes_atingido += 1
                    # Envia TCP apenas se for hit ou destroyed
                    threading.Thread(
                        target=enviar_resposta_tcp, 
                        args=(autor, resultado, x, y, PLAYER_ID), 
                        daemon=True
                    ).start()

            fila_rede.task_done()
    except queue.Empty:
        pass

    # Eventos Pygame
    for evento in pygame.event.get():
        if evento.type == pygame.QUIT:
            rodando = False
        elif evento.type == pygame.MOUSEBUTTONDOWN:
            # Scroll do log
            if interface.mouse_sobre_log(evento.pos):
                if evento.button == 4:  # scroll para cima
                    interface.mover_scroll(-1)
                elif evento.button == 5:  # scroll para baixo
                    interface.mover_scroll(1)
            # Clique no botão Sair
            elif evento.button == 1:
                if botao_sair_rect.collidepoint(evento.pos):
                    interface.adicionar_log("[BOTÃO] Saindo do jogo...")
                    try:
                        enviar_saida()
                    except Exception:
                        pass
                    rodando = False
        elif evento.type == pygame.MOUSEMOTION:
            interface.atualizar_hover(evento.pos)

    # Tiros automáticos (intervalo randomizado)
    tempo_tiro += clock.tick(60)
    if tempo_tiro >= random.randint(intervalo_tiro_min, intervalo_tiro_max) and posicoes_disponiveis and any(active_opponents.values()):
        x, y = posicoes_disponiveis.pop()
        enviar_tiro(x, y)
        meus_tiros_enviados[(x, y)] = {"opponent": OPPONENT_ID, "status": "pendente"}
        tempo_tiro = 0
        eventos_de_jogo = True

    # Redesenho
    if eventos_de_jogo or necessita_redesenho:
        tela.fill(COR_FUNDO)
        tabuleiro.desenhar(tela)
        interface.desenhar_log(tela)
        interface.desenhar_hud(tela)

        # Botão de saída
        pos_mouse = pygame.mouse.get_pos()
        cor_botao = COR_BOTAO_HOVER if botao_sair_rect.collidepoint(pos_mouse) else COR_BOTAO
        pygame.draw.rect(tela, cor_botao, botao_sair_rect, border_radius=12)
        texto_sair = FONTE_BOTAO.render("Sair do Jogo", True, COR_TEXTO_BOTAO)
        tela.blit(texto_sair, (botao_sair_rect.centerx - texto_sair.get_width() // 2,
                               botao_sair_rect.centery - texto_sair.get_height() // 2))

        interface.atualizar_hover(pos_mouse)
        pygame.display.flip()
        necessita_redesenho = False

    # --- DERROTA ---
    if tabuleiro.todos_destruidos() and rodando:
        interface.adicionar_log(f"[DERROTA] Jogador {PLAYER_ID} foi derrotado!")

        # Envia DERROTA repetidamente
        for _ in range(3):
            try:
                enviar_derrota()
                interface.adicionar_log("[UDP ENVIO DERROTA] LOST enviado")
            except Exception:
                pass
            pygame.time.wait(500)

        active_opponents = {k: False for k in active_opponents}

        # Envia SAINDO
        try:
            enviar_saida()
            interface.adicionar_log("[UDP ENVIO SAIDA] SAINDO enviado")
        except Exception:
            pass
        active_opponents = {k: False for k in active_opponents}
        pygame.time.wait(2000)
        rodando = False

    # --- VITÓRIA ---
    total_acertos_necessarios = 14
    if jogadores[0]["acertos"] >= total_acertos_necessarios and rodando:
        interface.adicionar_log(f"[VITÓRIA] Jogador {PLAYER_ID} venceu!")
        # Envia SAINDO
        try:
            enviar_saida()
        except Exception:
            pass
        pygame.time.wait(2000)
        rodando = False

pygame.quit()

# --- Envia SAINDO caso não tenha enviado ainda ---
try:
    enviar_saida()
except Exception:
    pass

# --- Score final ---
unique_opponents_hit = len([p for p, cnt in hits_by_player.items() if cnt > 0])
score_final = unique_opponents_hit - (vezes_atingido or 0)

print("\n===== SCORE FINAL =====")
print(f"Você foi atingido {vezes_atingido} vezes")
for p, cnt in hits_by_player.items():
    print(f"Você atingiu o jogador {p} {cnt} vezes")
print(f"Score final (players_hit - times_hit): {unique_opponents_hit} - {vezes_atingido} = {score_final}")
