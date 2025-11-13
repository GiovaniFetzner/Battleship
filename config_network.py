import socket
import threading
import time
import os
import ast

# --- Configurações ---
UDP_PORT = int(os.getenv("PORT_UDP", 5000))
TCP_PORT = int(os.getenv("PORT_TCP", 5001))
BROADCAST_MSG = "Conectando"
BROADCAST_INTERVAL = 1
DISCOVERY_TIMEOUT = 3

# --- Variáveis globais ---
HOSTS = []  # lista de participantes
PLAYER_ID = None  # será definido dinamicamente
LOCK = threading.Lock()
LOCAL_IP = os.getenv("LOCAL_IP", "0.0.0.0")  # bind do servidor

# --- Função utilitária para adicionar host ---
def add_host(ip, porta, nome=None):
    with LOCK:
        if not any(h["ip"] == ip and h["porta"] == porta for h in HOSTS):
            HOSTS.append({"ip": ip, "porta": porta, "nome": nome or ip})

# --- UDP listener ---
def listen_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_IP, UDP_PORT))
    print(f"[NET] Escutando UDP {LOCAL_IP}:{UDP_PORT}")
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            if msg.startswith(BROADCAST_MSG):
                sender = msg.split(":")[1]
                add_host(addr[0], UDP_PORT, f"Jogador{sender}")
                # responde via TCP
                threading.Thread(target=respond_tcp, args=(addr[0], TCP_PORT), daemon=True).start()
        except Exception as e:
            print("[ERRO UDP]", e)

# --- TCP listener ---
def listen_tcp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_IP, TCP_PORT))
    sock.listen()
    print(f"[NET] Escutando TCP {LOCAL_IP}:{TCP_PORT}")
    while True:
        try:
            conn, addr = sock.accept()
            data = conn.recv(1024).decode().strip()
            if data.startswith("participantes:"):
                ip_list = ast.literal_eval(data.split(":", 1)[1])
                for host in ip_list:
                    add_host(host, UDP_PORT)
            conn.close()
        except Exception as e:
            print("[ERRO TCP]", e)

# --- Broadcast UDP ---
def broadcast_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    while True:
        msg = f"{BROADCAST_MSG}:1"  # ID fixo temporário
        with LOCK:
            for h in HOSTS:
                sock.sendto(msg.encode(), (h["ip"], UDP_PORT))
        time.sleep(BROADCAST_INTERVAL)

# --- Responde via TCP ---
def respond_tcp(target_ip, target_port):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((target_ip, target_port))
        with LOCK:
            nomes = [h["nome"] for h in HOSTS]
            sock.sendall(f"participantes:{nomes}".encode())
        sock.close()
    except Exception:
        pass  # normal se outro jogador ainda não estiver escutando

# --- Inicialização ---
def init_network():
    # adiciona o próprio host na lista
    add_host(LOCAL_IP, UDP_PORT, "Jogador1")
    threading.Thread(target=listen_udp, daemon=True).start()
    threading.Thread(target=listen_tcp, daemon=True).start()
    threading.Thread(target=broadcast_udp, daemon=True).start()
    time.sleep(DISCOVERY_TIMEOUT)

    # atribui PLAYER_ID ordenado por nome
    sorted_hosts = sorted(HOSTS, key=lambda h: h["nome"])
    global PLAYER_ID
    for i, h in enumerate(sorted_hosts, start=1):
        if h["nome"] == "Jogador1":
            PLAYER_ID = i
            break

    print(f"[INFO] IP local: {LOCAL_IP}")
    print(f"[INFO] PLAYER_ID: {PLAYER_ID}")
    print("[INFO] HOSTS detectados:")
    for h in HOSTS:
        print(f" - {h['nome']} ({h['ip']}:{h['porta']})")
