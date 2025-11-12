import socket
import threading
import time
import ast

# --- Configurações de portas e mensagens ---
UDP_PORT = 5000
TCP_PORT = 5001
BROADCAST_MSG = "Conectando"
BROADCAST_INTERVAL = 1  # segundos entre broadcasts
DISCOVERY_TIMEOUT = 3   # segundos para aguardar respostas

# --- Variáveis globais ---
HOSTS = []       # Lista de participantes: {"ip": ..., "porta": ...}
PLAYER_ID = None  # Será definido automaticamente

# --- Descobre IP local da máquina ---
def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Conecta a um IP qualquer da internet sem enviar dados
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip

LOCAL_IP = get_local_ip()
LOCAL_SUBNET = '.'.join(LOCAL_IP.split('.')[:3])  # filtra apenas a mesma sub-rede

# --- Thread para ouvir broadcasts UDP ---
def listen_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', UDP_PORT))
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode().strip()
            ip = addr[0]
            if msg == BROADCAST_MSG and ip != LOCAL_IP:
                # Filtra apenas IPs da mesma sub-rede
                if ip.startswith(LOCAL_SUBNET) and not any(h["ip"] == ip for h in HOSTS):
                    HOSTS.append({"ip": ip, "porta": UDP_PORT})
                # Responde via TCP com a lista de participantes
                threading.Thread(target=respond_tcp, args=(ip,), daemon=True).start()
        except Exception as e:
            print("[ERRO UDP]", e)

# --- Thread para ouvir conexões TCP ---
def listen_tcp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', TCP_PORT))
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
            print("[ERRO TCP]", e)

# --- Envia broadcast UDP ---
def broadcast_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    while True:
        sock.sendto(BROADCAST_MSG.encode(), ('<broadcast>', UDP_PORT))
        time.sleep(BROADCAST_INTERVAL)

# --- Responde via TCP com lista de participantes ---
def respond_tcp(target_ip):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((target_ip, TCP_PORT))
        ips = [h["ip"] for h in HOSTS] + [LOCAL_IP]
        msg = f"participantes:{ips}"
        sock.sendall(msg.encode())
        sock.close()
    except Exception:
        pass  # conexão falhou, ignora

# --- Inicialização da rede ---
def init_network():
    # Adiciona self na lista de HOSTS
    HOSTS.append({"ip": LOCAL_IP, "porta": UDP_PORT})

    # Inicia threads de escuta UDP e TCP
    threading.Thread(target=listen_udp, daemon=True).start()
    threading.Thread(target=listen_tcp, daemon=True).start()

    # Envia broadcast por alguns segundos
    end_time = time.time() + DISCOVERY_TIMEOUT
    broadcast_thread = threading.Thread(target=broadcast_udp, daemon=True)
    broadcast_thread.start()
    while time.time() < end_time:
        time.sleep(0.1)

    # Ordena HOSTS e define PLAYER_ID
    HOSTS.sort(key=lambda h: h["ip"])
    global PLAYER_ID
    for i, h in enumerate(HOSTS, start=1):
        if h["ip"] == LOCAL_IP:
            PLAYER_ID = i
            break

# --- Executa inicialização ---
init_network()

print(f"[INFO] IP local: {LOCAL_IP}")
print(f"[INFO] PLAYER_ID: {PLAYER_ID}")
print(f"[INFO] HOSTS detectados:")
for h in HOSTS:
    print(f"   - {h['ip']}:{h['porta']}")
