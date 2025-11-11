MODO_TESTE_LOCAL = True  # ← mude para False quando for jogar em máquinas diferentes

if MODO_TESTE_LOCAL:
    # modo local: portas separadas para evitar conflito no Windows
    UDP_PORT_SERVER = 5000
    TCP_PORT_SERVER = 5001
    UDP_PORT_CLIENT = 5002
    TCP_PORT_CLIENT = 5003
else:
    # modo rede real: mesmas portas para ambos
    UDP_PORT_SERVER = UDP_PORT_CLIENT = 5000
    TCP_PORT_SERVER = TCP_PORT_CLIENT = 5001

BROADCAST_MSG = "Conectando"