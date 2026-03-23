# Battleship Frontend

Frontend web do projeto Battleship, em HTML, CSS e JavaScript puro.

## Navegacao

- README principal do repositorio: [../README.md](../README.md)
- README do backend: [../battleship_api/battleship/README.md](../battleship_api/battleship/README.md)

## Stack

- HTML5
- CSS3
- JavaScript (vanilla)
- WebSocket API (browser)

## Estrutura

```text
battleship_app/
|- index.html
|- game.html
|- css/
|  |- styles.css
|- js/
|  |- index.js
|  |- game.js
|- img/
|- package.json
```

## Fluxo atual da aplicacao

### index.html

- Usuario informa `playerName`
- Opcionalmente informa `gameId` para entrar em sala existente
- O frontend chama o backend REST:
    - `POST /api/game` para criar jogo
    - `POST /api/game/{gameId}/join` para entrar no jogo
- `playerName`, `gameId` e estado inicial ficam no `sessionStorage`

### game.html

- Le os dados do `sessionStorage`
- Abre WebSocket em:
    - `ws://localhost:8080/ws/game?gameId=<id>&playerName=<nome>`
- Renderiza HUD com status da sala e informacoes do jogador
- Renderiza tabuleiro 10x10
- Permite posicionamento visual de navios no cliente:
    - selecao de navio
    - rotacao
    - remocao pelo modo lixeira
- Ao receber evento de estado, consulta o estado via REST para sincronizar HUD

## O que ja esta pronto

- Criar ou entrar em sala pela interface
- Navegacao de index para game
- Layout da sala com HUD
- Tabuleiro funcional para interacao visual
- Conexao e leitura de eventos WebSocket

## O que ainda falta integrar com backend

- Enviar ataque real para o backend via WebSocket (`ATTACK`)
- Enviar posicionamento de navios via WebSocket (`PLACE_SHIP`)
- Enviar pronto para iniciar partida (`PLAYER_READY`)

## Como executar

```bash
cd battleship_app
npm install
npm run dev
```

Aplicacao em: http://localhost:3000

## Requisito para funcionamento completo

Backend deve estar em execucao em http://localhost:8080.
Consulte os contratos de API em [../battleship_api/battleship/README.md](../battleship_api/battleship/README.md).