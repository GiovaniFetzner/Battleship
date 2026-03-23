# Battleship API

Backend do projeto Battleship, implementado com Spring Boot.

## Navegacao

- README principal do repositorio: [../../README.md](../../README.md)
- README do frontend: [../../battleship_app/README.md](../../battleship_app/README.md)

## Stack

- Java 17
- Spring Boot 3.2.2
- Spring Web
- Spring WebSocket
- Maven
- JUnit 5 (spring-boot-starter-test)

## O que já está implementado

- Criacao de jogo via REST
- Entrada de segundo jogador via REST
- Consulta de estado do jogo via REST
- Canal WebSocket para mensagens de jogo
- Regras de dominio (turno, ataque, vitoria, posicionamento)
- Repositorio em memoria para jogos ativos

## Como executar

No Windows:

```bash
cd battleship_api/battleship
mvnw.cmd spring-boot:run
```

Alternativa Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw spring-boot:run
```

Servidor HTTP: http://localhost:8080

## API REST atual

Base path: `/api/game`

### Criar jogo

- Metodo: `POST /api/game`
- Body:

```json
{
  "playerName": "Player1"
}
```

### Entrar no jogo

- Metodo: `POST /api/game/{gameId}/join`
- Body:

```json
{
  "playerName": "Player2"
}
```

### Consultar estado

- Metodo: `GET /api/game/{gameId}?playerName=Player1`

## WebSocket atual

- Endpoint: `ws://localhost:8080/ws/game?gameId=<id>&playerName=<nome>`

### Mensagens inbound aceitas

Campos comuns:

```json
{
  "type": "ATTACK|PLACE_SHIP|PLAYER_READY",
  "gameId": "<id>",
  "playerName": "Player1"
}
```

#### ATTACK

```json
{
  "type": "ATTACK",
  "gameId": "<id>",
  "playerName": "Player1",
  "x": 4,
  "y": 7
}
```

#### PLACE_SHIP

```json
{
  "type": "PLACE_SHIP",
  "gameId": "<id>",
  "playerName": "Player1",
  "shipType": "battleship",
  "size": 4,
  "x": 0,
  "y": 0,
  "orientation": "HORIZONTAL"
}
```

#### PLAYER_READY

```json
{
  "type": "PLAYER_READY",
  "gameId": "<id>",
  "playerName": "Player1"
}
```

### Eventos outbound do backend

- `ATTACK_RESULT`
- `GAME_STATE_UPDATED`
- `PLAYER_READY`
- `GAME_START`
- `SHIP_PLACED`
- `ERROR`

## CORS e integracao com frontend

- CORS permitido para `http://localhost:3000`
- Frontend esperado em: [../../battleship_app/readme.MD](../../battleship_app/readme.MD)

## Testes

Executar testes:

```bash
cd battleship_api/battleship
mvnw.cmd clean test
```

Ultimo resultado local desta atualizacao: 75 testes, 0 falhas, 0 erros, 0 ignorados.

