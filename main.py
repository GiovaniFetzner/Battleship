import pygame
import random
import threading
import queue
import time

from front.interface import Interface, LARGURA_TELA, ALTURA_TELA, COR_FUNDO
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes
from config_network import (
    HOSTS, PLAYER_ID, OPPONENT_IP, IS_DOCKER,
    enviar_tiro_para_todos, enviar_resposta_tcp, Jogador
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

# --- DEBUG: log das posições locais dos barcos ---
# Isso ajuda a diagnosticar discrepâncias entre Host e Container
try:
    interface.adicionar_log("[DEBUG] Posicionamento local dos barcos:")
    for b in tabuleiro.barcos:
        s = f"  {b.nome}: {b.get_posicoes()}"
        interface.adicionar_log(s)
except Exception:
    # Em casos headless ou durante testes, garantir que falhas de log
    # não quebrem o fluxo do jogo
    print("[DEBUG] Falha ao registrar posicoes dos barcos")

# --- Interface ---
jogadores = [
    {"nome": f"Jogador {PLAYER_ID} (Você)", "acertos": 0},
    {"nome": f"Adversário ({OPPONENT_IP})", "acertos": 0}
]
interface = Interface()
interface.atualizar_jogadores(jogadores)

# --- Fila de rede ---
fila_rede = queue.Queue()

# ============================================================
# REDE: usa a classe Jogador (centraliza lógica de rede)
# ============================================================

jugador_rede = Jogador()


def tratar_mensagem(msg):
    parts = msg.split(',')
    if len(parts) >= 4:
        try:
            x = int(parts[1])
            y = int(parts[2])
            autor = int(parts[3])
            fila_rede.put({"tipo": "tiro", "x": x, "y": y, "autor": autor})
        except ValueError:
            print("[REDE] Mensagem TIRO inválida:", msg)


def tratar_resposta_tcp(msg):
    """Callback para mensagens RES:resultado,x,y,autor.
    
    IMPORTANTE: Aqui contamos SEUS acertos!
    Quando você recebe uma resposta TCP confirmando que seu tiro acertou,
    incrementamos o contador de acertos do Jogador (você).
    """
    global meus_tiros_enviados
    
    try:
        parts = msg.split(':')[1].split(',')
        resultado, x, y, autor = parts[0], int(parts[1]), int(parts[2]), int(parts[3])
        interface.adicionar_log(f"[TCP-RECEBIDO] {resultado.upper()} em ({x},{y}) de J{autor}")
        
        # Se foi um acerto (hit ou destroyed), incrementar SEUS acertos
        if resultado.lower() in ("hit", "destroyed"):
            # Verificar se este é realmente um tiro que você enviou
            pos = (x, y)
            if pos in meus_tiros_enviados:
                # Só incrementar UMA VEZ (caso receba resposta duplicada)
                if meus_tiros_enviados[pos] == "pendente":
                    jogadores[0]["acertos"] += 1  # Você marcou um acerto!
                    meus_tiros_enviados[pos] = "acertou"
                    interface.atualizar_jogadores(jogadores)
    except Exception as e:
        print("[ERRO CALLBACK TCP]", e, msg)


# inicia a rede (registra callbacks e thread de escuta)
jugador_rede.start_network(on_tiro=tratar_mensagem, on_res_tcp=tratar_resposta_tcp)


# ============================================================
# FUNÇÕES DE JOGO
# ============================================================

def enviar_tiro(x, y):
    msg = f"TIRO,{x},{y},{PLAYER_ID}"
    enviar_tiro_para_todos(msg)
    interface.adicionar_log(f"[ENVIO] TIRO ({x},{y}) enviado por J{PLAYER_ID}")


# --- Rastreamento de tiros para contagem correta ---
# Cada posição (x,y) que você atira é armazenada para verificar depois na resposta TCP
meus_tiros_enviados = {}  # {(x, y): "pendente"} ou {(x, y): "acertou"}

# --- Lista de posições aleatórias ---
posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
random.shuffle(posicoes_disponiveis)
intervalo_tiro = 2000  # ms
tempo_tiro = 0

# ============================================================
# LOOP PRINCIPAL - Event-driven (redesenha apenas quando há eventos)
# ============================================================

rodando = True
necessita_redesenho = True  # Redesenha na primeira iteração

while rodando:
    # Processa eventos de rede
    eventos_de_jogo = False
    try:
        while True:
            evento_rede = fila_rede.get_nowait()
            eventos_de_jogo = True
            if evento_rede["tipo"] == "tiro":
                x, y, autor = evento_rede["x"], evento_rede["y"], evento_rede["autor"]

                resultado = tabuleiro.receber_tiro(x, y)
                interface.adicionar_log(f"[REDE] Tiro de J{autor} em ({x},{y}) -> {resultado}")

                if resultado in ("hit", "destroyed"):
                    jogadores[1]["acertos"] += 1  # Adversário marcou um acerto
                    interface.atualizar_jogadores(jogadores)
                    # Enviar resposta em thread separada para não bloquear o Pygame
                    threading.Thread(
                        target=enviar_resposta_tcp,
                        args=(autor, resultado, x, y, PLAYER_ID),
                        daemon=True
                    ).start()

            fila_rede.task_done()
    except queue.Empty:
        pass

    # Processa eventos do Pygame
    for evento in pygame.event.get():
        if evento.type == pygame.QUIT:
            rodando = False

    # Envia tiros a cada intervalo de tempo
    tempo_tiro += clock.tick(60)  # Limita a 60 FPS para não usar 100% CPU
    if tempo_tiro >= intervalo_tiro and posicoes_disponiveis:
        x, y = posicoes_disponiveis.pop()
        enviar_tiro(x, y)
        # Registrar este tiro no rastreamento (pendente de resposta)
        meus_tiros_enviados[(x, y)] = "pendente"
        tempo_tiro = 0
        eventos_de_jogo = True

    # Redesenha apenas se houver eventos ou tiros
    if eventos_de_jogo or necessita_redesenho:
        tela.fill(COR_FUNDO)
        tabuleiro.desenhar(tela)
        interface.desenhar_log(tela)
        interface.desenhar_hud(tela)
        pygame.display.flip()
        necessita_redesenho = False

    if tabuleiro.todos_destruidos():
        interface.adicionar_log(f"[DERROTA] Jogador {PLAYER_ID} foi derrotado!")
        pygame.time.wait(3000)
        rodando = False
    
    # Verifica se você ganhou (acertou em todos os 14 pontos do adversário)
    total_acertos_necessarios = 14  # PortaAvioes(5) + Bombardeiro(4) + Submarino(3) + Lancha(2)
    if jogadores[0]["acertos"] >= total_acertos_necessarios:
        interface.adicionar_log(f"[VITÓRIA] Jogador {PLAYER_ID} venceu!")
        pygame.time.wait(3000)
        rodando = False

pygame.quit()
