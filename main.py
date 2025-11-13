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
    enviar_tiro_para_todos, enviar_resposta_tcp, Jogador, enviar_saida
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
    print("[DEBUG] Posicionamento local dos barcos:")
    for b in tabuleiro.barcos:
        s = f"  {b.nome}: {b.get_posicoes()}"
        print(s)
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


def handle_ctrl_c(signum, frame):
    """Handler para Ctrl+C - envia SAINDO via UDP e encerra o jogo."""
    global rodando
    print("\n[CTRL+C] Encerrando jogo...")
    interface.adicionar_log("[CTRL+C] Você pressionou Ctrl+C - encerrando")
    
    # Enviar mensagem SAINDO para os demais participantes
    try:
        enviar_saida()
        print("[CTRL+C] Mensagem SAINDO enviada aos adversários")
    except Exception as e:
        print(f"[CTRL+C] Erro ao enviar SAINDO: {e}")
    
    # Parar o loop de jogo
    rodando = False


# Registrar o handler para SIGINT (Ctrl+C)
signal.signal(signal.SIGINT, handle_ctrl_c)



def tratar_mensagem(msg):
    # Tratar mensagens UDP variadas: TIRO,x,y,autor  ou  SAINDO,<id>  ou 'saindo'
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
        # Mensagem de saída: SAINDO,<player_id>
        try:
            leaving_id = int(parts[1]) if len(parts) >= 2 else OPPONENT_ID
            active_opponents[leaving_id] = False
            interface.adicionar_log(f"[NET] Jogador {leaving_id} saiu — não enviaremos mais mensagens para ele")
        except Exception:
            print("[REDE] Mensagem SAINDO inválida:", msg)
    elif msg.strip().lower() == "saindo":
        # caso simples sem id
        active_opponents[OPPONENT_ID] = False
        interface.adicionar_log(f"[NET] Jogador {OPPONENT_ID} saiu (mensagem 'saindo')")


def tratar_resposta_tcp(msg):
    """Callback para mensagens RES:resultado,x,y,autor.
    
    IMPORTANTE: Aqui contamos SEUS acertos!
    Quando você recebe uma resposta TCP confirmando que seu tiro acertou,
    incrementamos o contador de acertos do Jogador (você).
    """
    global meus_tiros_enviados, hits_by_player

    try:
        parts = msg.split(':')[1].split(',')
        resultado, x, y, autor = parts[0], int(parts[1]), int(parts[2]), int(parts[3])
        interface.adicionar_log(f"[TCP-RECEBIDO] {resultado.upper()} em ({x},{y}) de J{autor}")

        # Se foi um acerto (hit ou destroyed), verificar se é resposta ao nosso tiro
        if resultado.lower() in ("hit", "destroyed"):
            pos = (x, y)
            if pos in meus_tiros_enviados:
                entry = meus_tiros_enviados[pos]
                # entry is a dict: {"opponent": id, "status": "pendente"/"acertou"}
                if entry.get("status") == "pendente":
                    # Atualiza HUD/contagem de acertos do jogador local
                    jogadores[0]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)

                    # Marca como acertado e atualiza estatísticas por jogador
                    entry["status"] = "acertou"
                    opp = entry.get("opponent")
                    if opp is not None:
                        hits_by_player.setdefault(opp, 0)
                        hits_by_player[opp] += 1
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
meus_tiros_enviados = {}  # {(x,y): {"opponent": id, "status": "pendente"}}

# Ativos: controla para quais jogadores ainda podemos enviar mensagens
# Por padrão, o adversário padrão está ativo
OPPONENT_ID = 2 if PLAYER_ID == 1 else 1
active_opponents = {OPPONENT_ID: True}

# Estatísticas
vezes_atingido = 0
hits_by_player = {}  # {player_id: count}

# --- Lista de posições aleatórias ---
posicoes_disponiveis = [(x, y) for x in range(tabuleiro.colunas) for y in range(tabuleiro.linhas)]
random.shuffle(posicoes_disponiveis)
intervalo_tiro = 2000  # ms
tempo_tiro = 0

# Verificação inicial: não começar a atirar se estiver sem adversários
print(f"[INICIO] Adversários ativos: {active_opponents}")
tem_adversarios_ativos_inicial = any(active_opponents.values())
if not tem_adversarios_ativos_inicial:
    print("[AVISO] Nenhum adversário ativo! Aguardando adversários...")
    interface.adicionar_log("[AVISO] Nenhum adversário ativo - aguardando conexão")

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
                    # Adversário marcou um acerto
                    jogadores[1]["acertos"] += 1
                    interface.atualizar_jogadores(jogadores)
                    # Estatística: quantas vezes fui atingido
                    vezes_atingido += 1
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
    # Verificar se há adversários ativos antes de enviar tiro
    tem_adversarios_ativos = any(active_opponents.values())
    if tempo_tiro >= intervalo_tiro and posicoes_disponiveis and tem_adversarios_ativos:
        x, y = posicoes_disponiveis.pop()
        enviar_tiro(x, y)
        # Registrar este tiro no rastreamento (pendente de resposta)
        meus_tiros_enviados[(x, y)] = {"opponent": OPPONENT_ID, "status": "pendente"}
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
        # Enviar SAINDO antes de terminar
        try:
            enviar_saida()
        except Exception:
            pass
        pygame.time.wait(3000)
        rodando = False
    
    # Verifica se você ganhou (acertou em todos os 14 pontos do adversário)
    total_acertos_necessarios = 14  # PortaAvioes(5) + Bombardeiro(4) + Submarino(3) + Lancha(2)
    if jogadores[0]["acertos"] >= total_acertos_necessarios:
        interface.adicionar_log(f"[VITÓRIA] Jogador {PLAYER_ID} venceu!")
        # Enviar SAINDO antes de terminar
        try:
            enviar_saida()
        except Exception:
            pass
        pygame.time.wait(3000)
        rodando = False

    # Saída manual via janela: se o usuário fechar, enviar mensagem SAINDO
    # (também tratamos a finalização depois do loop)

pygame.quit()
# Ao encerrar, enviar mensagem SAINDO para o adversário (caso não tenha sido enviado já)
try:
    enviar_saida()
except Exception:
    pass

# Calcular score final: número de jogadores atingidos (único) - vezes_atingido
# Em um jogo com apenas um adversário, 'jogadores atingidos' será 1 se houver
# ao menos um hit naquele adversário.
unique_opponents_hit = len([p for p, cnt in hits_by_player.items() if cnt > 0])
score_final = unique_opponents_hit - (vezes_atingido or 0)

print("\n===== SCORE FINAL =====")
print(f"Você foi atingido {vezes_atingido} vezes")
for p, cnt in hits_by_player.items():
    print(f"Você atingiu o jogador {p} {cnt} vezes")
print(f"Score final (players_hit - times_hit): {unique_opponents_hit} - {vezes_atingido} = {score_final}")

# Estatísticas overall
total_hits_acertados = sum(hits_by_player.values()) if hits_by_player else 0
print("\n===== ESTATÍSTICAS GERAIS =====")
print(f"Total de vezes que você foi atingido: {vezes_atingido}")
print(f"Total de tiros seus que acertaram (overall): {total_hits_acertados}")
