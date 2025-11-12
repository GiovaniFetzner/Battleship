import socket

# --- Configuração da rede entre computadores diferentes ---
print("[CONFIG] Modo REDE REAL - execução em máquinas diferentes")

UDP_PORT_SERVER = UDP_PORT_CLIENT = 5000
TCP_PORT_SERVER = TCP_PORT_CLIENT = 5001

# --- Configure aqui os IPs reais de cada computador ---
HOSTS = [
    {"nome": "jogador1", "ip": "192.168.0.10", "porta": 5000},
    {"nome": "jogador2", "ip": "192.168.0.11", "porta": 5000}
    ]

# --- Determinar o ID do jogador ---
hostname = socket.gethostname().lower()
PLAYER_ID = 1  # padrão
for i, h in enumerate(HOSTS, start=1):
    if h["nome"] in hostname:
        PLAYER_ID = i
        break
else:
    print("[WARN] PLAYER_ID não identificado automaticamente. Usando jogador 1 por padrão.")

print(f"[INFO] Ambiente: REDE REAL | Player ID: {PLAYER_ID} | Hostname: {hostname}")
print(f"[INFO] Hosts configurados:")
for h in HOSTS:
    print(f"   - {h['nome']} -> {h['ip']}:{h['porta']}")

BROADCAST_MSG = "Conectando"
