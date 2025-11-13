import socket
import threading
import time
import ast
import sys

# --- Configurações ---
UDP_PORT = 5000
TCP_PORT = 5001
BROADCAST_MSG = "Conectando"
BROADCAST_INTERVAL = 1
DISCOVERY_TIMEOUT = 3

# --- Modo simulação ---
# Rode o script duas vezes, passando "1" ou "2" como argumento:
#   python main.py 1
#   python main.py 2
SIMULATION_MODE = True
INSTANCE_ID = int(sys.argv[1]) if len(sys.argv) > 1 else 1
FAKE_IPS = ["127.0.0.1", "127.0.0.2"]
LOCAL_IP = FAKE_IPS[INSTANCE_ID - 1]
LOCAL_SUBNET = "127.0.0"  # todos 127.x.x.x pertencem à mesma "rede"

# --- Variáveis globais ---
HOSTS = []
PLAYER_ID = None

# --- Thread UDP ---
def listen_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_IP, UDP_PORT))
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            ip = addr[0]
            if msg == BROADCAST_MSG and ip != LOCAL_IP:
                if ip.startswith(LOCAL_SUBNET) and not any(h["ip"] == ip for h in HOSTS):
                    HOSTS.append({"ip": ip, "porta": UDP_PORT})
                threading.Thread(target=respond_tcp, args=(ip,), daemon=True).start()
        except Exception as e:
            print(f"[ERRO UDP {LOCAL_IP}]", e)

# --- Thread TCP ---
def listen_tcp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((LOCAL_IP, TCP_PORT))
    sock.listen()
    while True:
        try:
            conn, addr = sock.accept()
            data = conn.recv(1024).decode().strip()
            if data.startswith("participantes:"):
                ip_list = ast.literal_eval(data.split(":", 1)[1])
                for ip in ip_list:
                    if ip.startswith(LOCAL_SUBNET) and ip != LOCAL_IP:
                        if not any(h["ip"] == ip for h in HOSTS):
                            HOSTS.append({"ip": ip, "porta": UDP_PORT})
            conn.close()
        except Exception as e:
            print(f"[ERRO TCP {LOCAL_IP}]", e)

# --- Broadcast UDP ---
def broadcast_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    while True:
        sock.sendto(BROADCAST_MSG.encode(), ('<broadcast>', UDP_PORT))
        # também envia direto pro outro IP da simulação
        for ip in FAKE_IPS:
            if ip != LOCAL_IP:
                sock.sendto(BROADCAST_MSG.encode(), (ip, UDP_PORT))
        time.sleep(BROADCAST_INTERVAL)

# --- Responde via TCP ---
def respond_tcp(target_ip):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((target_ip, TCP_PORT))
        ips = [h["ip"] for h in HOSTS] + [LOCAL_IP]
        msg = f"participantes:{ips}"
        sock.sendall(msg.encode())
        sock.close()
    except Exception:
        pass

# --- Inicialização ---
def init_network():
    HOSTS.append({"ip": LOCAL_IP, "porta": UDP_PORT})
    threading.Thread(target=listen_udp, daemon=True).start()
    threading.Thread(target=listen_tcp, daemon=True).start()

    end_time = time.time() + DISCOVERY_TIMEOUT
    threading.Thread(target=broadcast_udp, daemon=True).start()
    while time.time() < end_time:
        time.sleep(0.1)

    HOSTS.sort(key=lambda h: h["ip"])
    global PLAYER_ID
    for i, h in enumerate(HOSTS, start=1):
        if h["ip"] == LOCAL_IP:
            PLAYER_ID = i
            break

# --- Execução ---
init_network()

print(f"[INFO] Instância {INSTANCE_ID}")
print(f"[INFO] IP local: {LOCAL_IP}")
print(f"[INFO] PLAYER_ID: {PLAYER_ID}")
print(f"[INFO] HOSTS detectados:")
for h in HOSTS:
    print(f"   - {h['ip']}:{h['porta']}")
