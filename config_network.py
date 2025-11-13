import socket
import threading
import time
import sys
import random
import os

# PLAYER_ID can come from command-line argument or from environment (used by Docker)
try:
    # prefer environment variable (docker-compose sets PLAYER_ID)
    env_pid = os.environ.get("PLAYER_ID")
    if env_pid is not None:
        PLAYER_ID = int(env_pid)
    else:
        PLAYER_ID = int(sys.argv[1])
except (IndexError, ValueError, TypeError):
    PLAYER_ID = 1

# Detectar se está rodando em Docker
IS_DOCKER = os.path.exists("/.dockerenv")

# Portas diferentes por jogador para evitar eco/reflexão
# Player 1 (host): escuta 5000, envia para 5002 (Player 2)
# Player 2 (container): escuta 5002, envia para 5000 (Player 1)
if PLAYER_ID == 1:
    GAME_PORT = 5000  # Escuta aqui
    OPPONENT_GAME_PORT = 5002  # Envia para Player 2 aqui
else:  # PLAYER_ID == 2
    GAME_PORT = 5002  # Escuta aqui
    OPPONENT_GAME_PORT = 5000  # Envia para Player 1 aqui

# TCP fixo (ambos escutam 5001)
TCP_PORT = 5001

# IP e porta do adversário
OPPONENT_ID = 2 if PLAYER_ID == 1 else 1
OPPONENT_TCP_PORT = 5001  # Ambos escutam TCP na mesma porta

if IS_DOCKER:
    # No container, o adversário é o host — permita sobrescrever via env var
    # (docker-compose pode passar OPPONENT_IP) e, por padrão, use host.docker.internal
    OPPONENT_IP = os.environ.get("OPPONENT_IP", "host.docker.internal")
    # Para UDP (tiros), use gateway da rede Docker (172.20.0.1) que roteia para o host
    OPPONENT_UDP_IP = os.environ.get("OPPONENT_UDP_IP", "172.20.0.1")
    # Para TCP, use host.docker.internal (resolve para 192.168.65.254 no Windows)
    OPPONENT_TCP_IP = os.environ.get("OPPONENT_TCP_IP", "host.docker.internal")
    BIND_IP = "0.0.0.0"  # Escutar em todas as interfaces no Docker
else:
    # Execução local (host). Permitir sobrescrita via env var também.
    # IMPORTANTE: Use localhost:5001 pois o container mapeia para 0.0.0.0:5001
    # O IP interno 172.20.0.3 não é acessível do host no Windows
    OPPONENT_IP = os.environ.get("OPPONENT_IP", "localhost")
    OPPONENT_UDP_IP = os.environ.get("OPPONENT_UDP_IP", "127.0.0.1")
    OPPONENT_TCP_IP = os.environ.get("OPPONENT_TCP_IP", "localhost")
    BIND_IP = "127.0.0.1"  # Local escuta apenas em localhost

# Permitir sobrescrever BIND_IP via variável de ambiente (útil para desenvolvimento)
# Ex.: no host $env:BIND_IP = '0.0.0.0' para que o processo escute em todas as interfaces
BIND_IP = os.environ.get("BIND_IP", BIND_IP)

HOSTS = [{"ip": OPPONENT_IP, "porta": OPPONENT_GAME_PORT, "nome": f"Jogador{2 if PLAYER_ID == 1 else 1}"}]
LOCK = threading.Lock()
GAME_TCP_CALLBACK = None
LOCAL_IP = BIND_IP

def register_tcp_game_callback(cb):
    global GAME_TCP_CALLBACK
    GAME_TCP_CALLBACK = cb
    print(f"[NET] Callback TCP registrado para Jogador {PLAYER_ID}")

def listen_tcp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
    # Bind em 0.0.0.0 para escutar em TODAS as interfaces (inclusive a rede Docker)
    bind_address = "0.0.0.0" if IS_DOCKER else BIND_IP
    sock.bind((bind_address, TCP_PORT))
    sock.listen(5)
    print(f"[NET] Jogador {PLAYER_ID} escutando TCP em {bind_address}:{TCP_PORT}")
    while True:
        try:
            conn, addr = sock.accept()
            data = conn.recv(4096).decode().strip()
            if data and data.startswith("RES:"):
                print(f"[TCP] Recebido de {addr}: {data}")
                if GAME_TCP_CALLBACK:
                    try:
                        GAME_TCP_CALLBACK(data)
                    except Exception as e:
                        print(f"[ERRO CALLBACK TCP] {e}")
            conn.close()
        except Exception as e:
            print(f"[ERRO TCP] {e}")

def ouvir_mensagens_de_jogo(callback):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((BIND_IP, GAME_PORT))
    print(f"[NET] Jogador {PLAYER_ID} escutando tiros em UDP porta {GAME_PORT}")
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            if msg.startswith("TIRO"):
                print(f"[UDP TIRO] Recebido de {addr}: {msg}")
                callback(msg)
        except Exception as e:
            print(f"[ERRO UDP TIRO] {e}")
            time.sleep(0.1)

def enviar_tiro_para_todos(msg):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.sendto(msg.encode(), (OPPONENT_UDP_IP, OPPONENT_GAME_PORT))
        sock.close()
        print(f"[UDP ENVIO] {msg} -> {OPPONENT_UDP_IP}:{OPPONENT_GAME_PORT}")
    except Exception as e:
        print(f"[ERRO ENVIO UDP] {e}")

def enviar_resposta_tcp(dest_id, resultado, x, y, autor):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((OPPONENT_TCP_IP, OPPONENT_TCP_PORT))
        msg = f"RES:{resultado},{x},{y},{autor}"
        sock.sendall(msg.encode())
        sock.close()
        print(f"[TCP ENVIO] {msg} -> {OPPONENT_TCP_IP}:{OPPONENT_TCP_PORT}")
    except Exception as e:
        print(f"[ERRO ENVIO TCP] {e}")

def init_network():
    threading.Thread(target=listen_tcp, daemon=True).start()
    time.sleep(0.5)

print(f"\n[INFO] ===== JOGADOR {PLAYER_ID} =====")
print(f"[INFO] Ambiente: {'DOCKER' if IS_DOCKER else 'LOCAL'}")
print(f"[INFO] Escutando tiros em UDP {BIND_IP}:{GAME_PORT}")
print(f"[INFO] Escutando respostas em TCP {BIND_IP}:{TCP_PORT}")
print(f"[INFO] Adversário: {OPPONENT_IP}")
print(f"[INFO] Enviando tiros para: UDP {OPPONENT_IP}:5000")
print(f"[INFO] Enviando respostas para: TCP {OPPONENT_IP}:5001")
print(f"[INFO] ========================\n")

init_network()

class Jogador:
    def __init__(self):
        self._auto_thread = None
        self._auto_stop = threading.Event()

    def start_network(self, on_tiro, on_res_tcp):
        register_tcp_game_callback(on_res_tcp)
        t = threading.Thread(target=lambda: ouvir_mensagens_de_jogo(on_tiro), daemon=True)
        t.start()

    def start_auto_shots(self, interval_s=2):
        if self._auto_thread and self._auto_thread.is_alive():
            return
        def _loop():
            while not self._auto_stop.wait(interval_s):
                x = random.randint(0, 9)
                y = random.randint(0, 9)
                msg = f"TIRO,{x},{y},{PLAYER_ID}"
                try:
                    enviar_tiro_para_todos(msg)
                except Exception:
                    pass
        self._auto_stop.clear()
        self._auto_thread = threading.Thread(target=_loop, daemon=True)
        self._auto_thread.start()

    def stop_auto_shots(self):
        self._auto_stop.set()
