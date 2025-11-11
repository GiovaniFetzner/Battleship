import socket
import threading
import time
from config_network import *

participantes = set()

def send_tcp_raw(ip, port, data: bytes):
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(3)
            s.connect((ip, port))
            s.sendall(data)
        print(f"[TCP] Enviado para {ip}:{port} -> {data!r}")
    except Exception as e:
        print(f"[TCP] Erro ao enviar para {ip}:{port} -> {e}")

def tcp_listener():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('', TCP_PORT_CLIENT))
    s.listen()
    print(f"[TCP] Listener do client rodando na porta {TCP_PORT_CLIENT}")

    while True:
        conn, addr = s.accept()
        try:
            data = b''
            chunk = conn.recv(4096)
            while chunk:
                data += chunk
                if len(chunk) < 4096:
                    break
                chunk = conn.recv(4096)
            if data:
                print(f"[TCP] Recebido de {addr}: {data!r}")
        finally:
            conn.close()

def udp_listener():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(('', UDP_PORT_CLIENT))
    print(f"[UDP] Listener do client rodando na porta {UDP_PORT_CLIENT}")

    while True:
        data, addr = s.recvfrom(4096)
        try:
            texto = data.decode('utf-8')
        except Exception:
            texto = None
        print(f"[UDP] Recebido de {addr}: {texto if texto is not None else data!r}")

        # registra IP de origem
        participantes.add(addr[0])

        # opcional: responder via TCP com uma mensagem simples
        # send_tcp_raw(addr[0], TCP_PORT_SERVER, b"OK from client")

def anunciar_broadcast():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    try:
        s.sendto(BROADCAST_MSG.encode('utf-8'), ('<broadcast>', UDP_PORT_SERVER))
        print(f"[UDP] Broadcast enviado para porta {UDP_PORT_SERVER}: {BROADCAST_MSG}")
    except Exception as e:
        print(f"[UDP] Falha ao enviar broadcast: {e}")
    finally:
        s.close()

if __name__ == "__main__":
    print(f"[START] Client usando UDP {UDP_PORT_CLIENT} / TCP {TCP_PORT_CLIENT}  (modo local={MODO_TESTE_LOCAL})")
    threading.Thread(target=tcp_listener, daemon=True).start()
    threading.Thread(target=udp_listener, daemon=True).start()

    time.sleep(0.2)
    anunciar_broadcast()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Encerrando client...")
