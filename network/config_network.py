import os

"""
Configuração de rede para a Batalha Naval
-----------------------------------------
Três modos de operação:

MODO_LOCAL
   - Usa portas diferentes para simular dois jogadores no mesmo PC.
   - Útil para testes rápidos (sem rede real).

MODO_DOCKER
   - Usa as mesmas portas, com broadcast local via rede Docker.
   - Cada contêiner representa um jogador.

MODO_REDE_REAL
   - Usa as mesmas portas, broadcast na LAN real.
   - Para apresentação com máquinas diferentes.

Você pode mudar o modo:
 - Editando a constante MODO abaixo, ou
 - Definindo a variável de ambiente MODO_EXEC (ex: "docker", "local", "rede")
"""

# Opções possíveis: "local", "docker", "rede"
MODO = os.getenv("MODO_EXEC", "local").lower()

if MODO == "local":
    print("[CONFIG] Modo LOCAL - simulação no mesmo PC")
    UDP_PORT_SERVER = 5000
    TCP_PORT_SERVER = 5001
    UDP_PORT_CLIENT = 5002
    TCP_PORT_CLIENT = 5003

elif MODO == "docker":
    print("[CONFIG] Modo DOCKER - múltiplos contêineres na mesma rede virtual")
    UDP_PORT_SERVER = UDP_PORT_CLIENT = 5000
    TCP_PORT_SERVER = TCP_PORT_CLIENT = 5001

elif MODO == "rede":
    print("[CONFIG] Modo REDE REAL - execução em máquinas diferentes")
    UDP_PORT_SERVER = UDP_PORT_CLIENT = 5000
    TCP_PORT_SERVER = TCP_PORT_CLIENT = 5001

else:
    raise ValueError(f"Modo inválido: {MODO}. Use 'local', 'docker' ou 'rede'.")

BROADCAST_MSG = "Conectando"
