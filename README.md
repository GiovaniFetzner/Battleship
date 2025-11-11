# Battleship

Para testar localhost a conexão e o jogo:

### config_network.py

```
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
```

| Máquina              | IP        | UDP  | TCP  |
| -------------------- | --------- | ---- | ---- |
| Servidor (Jogador A) | 127.0.0.1 | 5000 | 5001 |
| Cliente (Jogador B)  | 127.0.0.1 | 5002 | 5003 |

Assim, cada um:

* escuta em **suas próprias portas**, evitando conflito;
* envia mensagens UDP para o outro (usando broadcast ou diretamente);
* e pode se conectar via **TCP** para trocar listas de participantes ou iniciar o jogo.

## Por que a utilização de threads 

1. **Evita bloqueio do programa principal (responsividade)**

   * `socket.accept()` e `recv()` são chamadas **bloqueantes**. Se você as chamar no mesmo thread do loop do jogo (ou do terminal interativo), o programa "congela" enquanto espera rede. Threads fazem essas chamadas em paralelo, mantendo a interface (UI) ou lógica do jogo responsiva.

2. **Permite I/O concorrente**

   * Rede é um trabalho predominantemente *I/O-bound* (espera por rede). O Python libera o GIL durante operações de I/O, então **múltiplas threads I/O podem progredir em paralelo** sem serem bloqueadas pelo GIL — ganho real em throughput e latência para múltiplas conexões.

3. **Separação de responsabilidades / simplicidade**

   * Threads tornam o código mais simples: um thread cuida do TCP, outro do UDP, o main cuida do jogo (pygame). Isso facilita integrar rede e UI sem complexidade de eventos ou reescrever com `asyncio`.

4. **Melhor para latência interativa**

   * Receber mensagens (ataques, resultados) imediatamente e processá-las evita checagens contínuas (polling) no loop principal, reduzindo latência percebida.

---

## ⚙️ Resumo técnico 

| Protocolo | Papel    | Porta | Direção | Função                               |
| --------- | -------- | ----- | ------- | ------------------------------------ |
| **UDP**   | Cliente  | 5000  | Saída   | Broadcast “Conectando”               |
| **UDP**   | Servidor | 5000  | Entrada | Recebe broadcast de outros jogadores |
| **TCP**   | Servidor | 5001  | Entrada | Recebe lista de IPs                  |
| **TCP**   | Cliente  | 5001  | Saída   | Envia lista de IPs                   |

---