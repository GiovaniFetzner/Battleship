import socket
import threading
import time
import ast
import sys

# --- Configurações ---
BASE_UDP_PORT = 5000
BASE_TCP_PORT = 5002
BROADCAST_MSG = "Conectando"
BROADCAST_INTERVAL = 1
DISCOVERY_TIMEOUT = 3

# --- Modo simulação ---
INSTANCE_ID = int(sys.argv[1]) if len(sys.argv) > 1 else 1
FAKE_IPS = ["127.0.0.1", "127.0.0.1"]  # todos usam o mesmo IP real
FAKE_IDS = {1: "J1", 2: "J2"}
LOCAL_BIND_IP = "127.0.0.1"

# Cada instância usa portas diferentes
UDP_PORT = BASE_UDP_PORT + INSTANCE_ID - 1
TCP_PORT = BASE_TCP_PORT + INSTANCE_ID - 1

# --- Variáveis globais ---
HOSTS = []
PLAYER_ID = None
LOCK = threading.Lock()


# --- Função utilitária ---
def add_host(ip_fake, porta, nome=None):
    """Adiciona um host sem duplicar."""
    with LOCK:
        if not any(h["ip"] == ip_fake and h["porta"] == porta for h in HOSTS):
            HOSTS.append({
                "ip": ip_fake,
                "porta": porta,
                "nome": nome or ip_fake
            })


# --- Thread UDP ---
def listen_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_BIND_IP, UDP_PORT))
    print(f"[NET] Jogador {INSTANCE_ID} escutando UDP {LOCAL_BIND_IP}:{UDP_PORT}")

    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            if msg.startswith(BROADCAST_MSG):
                sender_id = int(msg.split(":")[1])
                if sender_id != INSTANCE_ID:
                    add_host(LOCAL_BIND_IP, BASE_UDP_PORT + sender_id - 1, f"Jogador{sender_id}")
                    # Responde via TCP com a lista de participantes
                    threading.Thread(target=respond_tcp, args=(sender_id,), daemon=True).start()
        except Exception as e:
            print(f"[ERRO UDP Jogador{INSTANCE_ID}]", e)


# --- Thread TCP ---
def listen_tcp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_BIND_IP, TCP_PORT))
    sock.listen()
    print(f"[NET] Jogador {INSTANCE_ID} escutando TCP {LOCAL_BIND_IP}:{TCP_PORT}")

    while True:
        try:
            conn, addr = sock.accept()
            data = conn.recv(1024).decode().strip()
            if data.startswith("participantes:"):
                ip_list = ast.literal_eval(data.split(":", 1)[1])
                for host in ip_list:
                    if host != f"Jogador{INSTANCE_ID}":
                        add_host(LOCAL_BIND_IP, BASE_UDP_PORT + int(host[-1]) - 1, host)
            conn.close()
        except Exception as e:
            print(f"[ERRO TCP Jogador{INSTANCE_ID}]", e)


# --- Broadcast UDP ---
def broadcast_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    while True:
        msg = f"{BROADCAST_MSG}:{INSTANCE_ID}"
        for target_id in range(1, len(FAKE_IPS) + 1):
            if target_id != INSTANCE_ID:
                target_port = BASE_UDP_PORT + target_id - 1
                sock.sendto(msg.encode(), (LOCAL_BIND_IP, target_port))
        time.sleep(BROADCAST_INTERVAL)


# --- Responde via TCP ---
def respond_tcp(target_id):
    """Envia a lista de hosts atuais via TCP para outro jogador."""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((LOCAL_BIND_IP, BASE_TCP_PORT + target_id - 1))
        with LOCK:
            nomes = [h["nome"] for h in HOSTS] + [f"Jogador{INSTANCE_ID}"]
        msg = f"participantes:{nomes}"
        sock.sendall(msg.encode())
        sock.close()
    except Exception:
        pass  # normal se o outro jogador ainda não estiver escutando


# --- Inicialização ---
def init_network():
    add_host(LOCAL_BIND_IP, UDP_PORT, f"Jogador{INSTANCE_ID}")

    threading.Thread(target=listen_udp, daemon=True).start()
    threading.Thread(target=listen_tcp, daemon=True).start()
    threading.Thread(target=broadcast_udp, daemon=True).start()

    time.sleep(DISCOVERY_TIMEOUT)

    # Atribui IDs ordenados por nome (Jogador1, Jogador2, etc.)
    sorted_hosts = sorted(HOSTS, key=lambda h: h["nome"])
    global PLAYER_ID
    for i, h in enumerate(sorted_hosts, start=1):
        if h["nome"] == f"Jogador{INSTANCE_ID}":
            PLAYER_ID = i
            break


# --- Execução ---
init_network()

print(f"[INFO] Instância {INSTANCE_ID}")
print(f"[INFO] IP local: {LOCAL_BIND_IP}")
print(f"[INFO] PLAYER_ID: {PLAYER_ID}")
print(f"[INFO] HOSTS detectados:")
for h in HOSTS:
    print(f"   - {h['nome']} ({h['ip']}:{h['porta']})")

LOCAL_IP = LOCAL_BIND_IP
