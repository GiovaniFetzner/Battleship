import socket
import threading
import time
import random
from config_network import *
from front.tabuleiro import Tabuleiro
from front.barcos.lancha import Lancha
from front.barcos.submarino import Submarino
from front.barcos.bombardeiro import Bombardeiro
from front.barcos.porta_avioes import PortaAvioes

participantes = set()
lock_participantes = threading.Lock()


def send_tcp_raw(ip, port, data: bytes):
    """Envia bytes via TCP para um IP."""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(3)
            s.connect((ip, port))
            s.sendall(data)
        print(f"[TCP] Enviado para {ip}:{port} -> {data!r}")
    except Exception as e:
        print(f"[TCP] Erro ao enviar para {ip}:{port} -> {e}")


def tcp_listener():
    """Escuta respostas TCP de outros jogadores."""
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(("", TCP_PORT_SERVER))
    s.listen()
    print(f"[TCP] Listener rodando na porta {TCP_PORT_SERVER}")

    while True:
        conn, addr = s.accept()
        try:
            data = conn.recv(4096)
            if data:
                print(f"[TCP] Recebido de {addr}: {data.decode('utf-8')}")
        finally:
            conn.close()


def udp_listener(tabuleiro):
    """Escuta mensagens UDP (tiros ou broadcasts)."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(("", UDP_PORT_SERVER))
    print(f"[UDP] Listener rodando na porta {UDP_PORT_SERVER}")

    while True:
        data, addr = s.recvfrom(4096)
        try:
            texto = data.decode("utf-8")
        except:
            continue

        ip = addr[0]
        with lock_participantes:
            participantes.add(ip)

        print(f"[UDP] Recebido de {ip}: {texto}")

        if texto.startswith("shot:"):
            try:
                x, y = map(int, texto.split(":")[1].split(","))
            except ValueError:
                continue

            resultado = tabuleiro.receber_tiro(x, y)
            print(f"[GAME] Tiro recebido em ({x},{y}) -> {resultado}")

            if resultado == "hit":
                send_tcp_raw(ip, TCP_PORT_SERVER, b"hit")
            elif resultado == "destroyed":
                send_tcp_raw(ip, TCP_PORT_SERVER, b"destroyed")

            if tabuleiro.todos_destruidos():
                print("[GAME] Todos os barcos foram destruídos! Enviando 'lost'...")
                broadcast_udp("lost")


def broadcast_udp(msg: str):
    """Envia mensagem UDP em broadcast."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.sendto(msg.encode("utf-8"), ("<broadcast>", UDP_PORT_SERVER))
    s.close()
    print(f"[UDP] Broadcast: {msg}")


def thread_disparos(tabuleiro):
    """Envia tiros automáticos a cada 10 segundos."""
    while True:
        time.sleep(10)
        with lock_participantes:
            alvos = list(participantes)

        if not alvos:
            continue

        x = random.randint(0, tabuleiro.colunas - 1)
        y = random.randint(0, tabuleiro.linhas - 1)
        mensagem = f"shot:{x},{y}".encode("utf-8")

        for ip in alvos:
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                s.sendto(mensagem, (ip, UDP_PORT_SERVER))
                s.close()
                print(f"[SHOT] Enviado {mensagem.decode()} para {ip}")
            except Exception as e:
                print(f"[UDP] Erro ao enviar tiro para {ip}: {e}")


def anunciar_broadcast():
    """Faz broadcast inicial para anunciar presença."""
    broadcast_udp(BROADCAST_MSG)


if __name__ == "__main__":
    print(f"[START] Jogador rodando nas portas UDP {UDP_PORT_SERVER} / TCP {TCP_PORT_SERVER}")

    tabuleiro = Tabuleiro()
    tipos_barcos = [PortaAvioes, Bombardeiro, Submarino, Lancha]
    tabuleiro.posicionar_barcos_automaticamente(tipos_barcos)

    threading.Thread(target=tcp_listener, daemon=True).start()
    threading.Thread(target=udp_listener, args=(tabuleiro,), daemon=True).start()
    threading.Thread(target=thread_disparos, args=(tabuleiro,), daemon=True).start()

    time.sleep(0.5)
    anunciar_broadcast()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Encerrando jogador...")
