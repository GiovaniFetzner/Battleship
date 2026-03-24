# Battleship Frontend

Frontend web do projeto Battleship, em HTML, CSS e JavaScript puro.

## Navegacao

- README principal: [../README.md](../README.md)
- README do backend: [../battleship_api/battleship/README.md](../battleship_api/battleship/README.md)

## Stack

- HTML5
- CSS3
- JavaScript (vanilla)
- WebSocket API (browser)
- serve (dev server local)

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

- Usuario informa playerName
- Pode informar gameId para entrar em sala existente
- Chama backend REST:
  - POST /api/game para criar jogo
  - POST /api/game/{gameId}/join para entrar
- Salva playerName, gameId e gameState inicial no sessionStorage

### game.html

- Le dados do sessionStorage
- Abre WebSocket em ws://localhost:8080/ws/game?gameId=<id>&playerName=<nome>
- Renderiza HUD e dois tabuleiros 10x10 (jogador e oponente)
- Permite posicionar navios com selecao, rotacao e remocao
- Envia posicionamentos via PLACE_SHIP e confirma pronto via PLAYER_READY
- Envia ataques no tabuleiro do oponente via ATTACK
- Processa eventos ATTACK_RESULT, PLAYER_READY, GAME_START, GAME_STATE_UPDATED e ERROR
- Sincroniza estado detalhado via GET /api/game/{gameId}?playerName=<nome>

## Recursos de UX atuais

- Indicador de status do WebSocket
- HUD de turno, status da partida, ataques e navios
- Transicao visual entre fases
- Botao para copiar gameId
- Overlay de fim de jogo com redirecionamento para inicio

## Como executar

```bash
cd battleship_app
npm install
npm run dev
```

Aplicacao: http://localhost:3000

## Dependencia do backend

Para fluxo completo, o backend deve estar ativo em http://localhost:8080.
Contratos de API: [../battleship_api/battleship/README.md](../battleship_api/battleship/README.md)