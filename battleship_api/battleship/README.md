# Battleship API

Backend do projeto Battleship, implementado com Spring Boot.

## Navegacao

- README principal: [../../README.md](../../README.md)
- README do frontend: [../../battleship_app/README.md](../../battleship_app/README.md)

## Stack

- Java 17
- Spring Boot 3.2.2
- Spring Web
- Spring WebSocket
- Maven
- JUnit 5 (spring-boot-starter-test)

## O que a API cobre hoje

- Criar jogo
- Entrar em jogo existente
- Consultar estado por jogador
- Receber e processar mensagens de partida via WebSocket
- Regras de dominio (turno, ataque, destruicao e vitoria)
- Armazenamento em memoria para partidas ativas

## Como executar

Windows:

```bash
cd battleship_api/battleship
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw spring-boot:run
```

Servidor: http://localhost:8080

## REST API

Base path: /api/game

### Criar jogo

- Metodo: POST /api/game
- Body:

```json
{
  "playerName": "Player1"
}
```

### Entrar no jogo

- Metodo: POST /api/game/{gameId}/join
- Body:

```json
{
  "playerName": "Player2"
}
```

### Consultar estado

- Metodo: GET /api/game/{gameId}?playerName=Player1

## WebSocket

- Endpoint: ws://localhost:8080/ws/game?gameId=<id>&playerName=<nome>

### Mensagens inbound

Campos comuns:

```json
{
  "type": "ATTACK|PLACE_SHIP|PLAYER_READY",
  "gameId": "<id>",
  "playerName": "Player1"
}
```

ATTACK:

```json
{
  "type": "ATTACK",
  "gameId": "<id>",
  "playerName": "Player1",
  "x": 4,
  "y": 7
}
```

PLACE_SHIP:

```json
{
  "type": "PLACE_SHIP",
  "gameId": "<id>",
  "playerName": "Player1",
  "shipType": "bombardeiro",
  "size": 4,
  "x": 0,
  "y": 0,
  "orientation": "HORIZONTAL"
}
```

PLAYER_READY:

```json
{
  "type": "PLAYER_READY",
  "gameId": "<id>",
  "playerName": "Player1"
}
```

### Eventos outbound

- ATTACK_RESULT
- GAME_STATE_UPDATED
- PLAYER_READY
- GAME_START
- SHIP_PLACED
- ERROR

## Integracao com frontend

- CORS HTTP liberado para http://localhost:3000
- WebSocket registrado em /ws/game
- Frontend esperado em [../../battleship_app/README.md](../../battleship_app/README.md)

## Testes

Windows:

```bash
cd battleship_api/battleship
mvnw.cmd clean test
```

Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw clean test
```

