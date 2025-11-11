import socket
import threading
import time
from config_network import *

participantes = set()

def send_tcp_raw(ip, port, data: bytes):
    """Conectar por TCP e enviar bytes brutos (usa timeout curto)."""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(3)
            s.connect((ip, port))
            s.sendall(data)
        print(f"[TCP] Enviado para {ip}:{port} -> {data!r}")
    except Exception as e:
        print(f"[TCP] Erro ao enviar para {ip}:{port} -> {e}")

def tcp_listener():
    """Aceita conexões TCP e imprime os bytes recebidos."""
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('', TCP_PORT_SERVER))
    s.listen()
    print(f"[TCP] Listener rodando na porta {TCP_PORT_SERVER}")

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
    """Escuta broadcasts UDP e imprime o conteúdo (string se decodificar)."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(('', UDP_PORT_SERVER))
    print(f"[UDP] Listener rodando na porta {UDP_PORT_SERVER}")

    while True:
        data, addr = s.recvfrom(4096)
        try:
            texto = data.decode('utf-8')
        except Exception:
            texto = None
        print(f"[UDP] Recebido de {addr}: {texto if texto is not None else data!r}")

        # registra IP de origem para futuros envios
        participantes.add(addr[0])
        
        mensagem_tcp = f"participantes: {participantes}"
        try:
            tcp_conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            tcp_conn.connect((addr[0], TCP_PORT_SERVER))  
            tcp_conn.sendall(mensagem_tcp.encode())
            tcp_conn.close()
            print(f"[TCP] Enviada lista para {addr[0]}: {mensagem_tcp}")
        except Exception as e:
            print(f"[ERRO TCP] Falha ao enviar lista para {addr[0]}: {e}")

def anunciar_broadcast():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    try:
        s.sendto(BROADCAST_MSG.encode('utf-8'), ('<broadcast>', UDP_PORT_CLIENT))
        print(f"[UDP] Broadcast inicial enviado para porta {UDP_PORT_CLIENT}: {BROADCAST_MSG}")
    except Exception as e:
        print(f"[UDP] Falha ao enviar broadcast: {e}")
    finally:
        s.close()

if __name__ == "__main__":
    print(f"[START] Servidor usando UDP {UDP_PORT_SERVER} / TCP {TCP_PORT_SERVER}  (modo local={MODO_TESTE_LOCAL})")
    threading.Thread(target=tcp_listener, daemon=True).start()
    threading.Thread(target=udp_listener, daemon=True).start()

    time.sleep(0.2)
    anunciar_broadcast()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Encerrando servidor...")
