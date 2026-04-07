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
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit 5 (spring-boot-starter-test)

## O que a API cobre hoje

- Criar jogo
- Entrar em jogo existente
- Consultar estado por jogador
- Receber e processar mensagens de partida via WebSocket
- Regras de dominio (turno, ataque, destruicao e vitoria)
- Armazenamento em memoria para partidas ativas no profile `dev` (em `prod`/`test`, persistencia via JPA)

## Camada de persistencia

- A persistencia foi organizada em modelo de portas e adaptadores para desacoplar dominio e infraestrutura.
- Portas de saida:
  - `repository/GameRepository` (acesso principal ao agregado de jogo no fluxo da aplicacao)
  - `service/persistence/port/GameStatePersistencePort` (snapshot de estado para salvar/recuperar)
- Adaptadores/implementacoes:
  - `repository/impl/InMemoryGameRepository` (`@Profile("dev")`) para desenvolvimento sem banco
  - `repository/impl/jpa/JpaGameRepository` (`@Profile({ "prod", "test" })`) para persistir o agregado com JPA
  - `persistence/adapter/JpaGameStatePersistenceAdapter` (`@Profile({ "prod", "test" })`) para implementar a porta de snapshot
- Mapeamento e modelo de persistencia:
  - DTOs de snapshot em `dto/persistence`
  - Entidades JPA em `persistence/entity` (`GameEntity`, `PlayerEntity`, `BoardEntity`, `AttackEntity`)
  - Mapper central em `persistence/mapper/GameEntityMapper`
- Profiles e banco:
  - `dev`: sem datasource/JPA (autoconfiguracoes JDBC/JPA desativadas em `application-dev.properties`)
  - `prod`: PostgreSQL (`application-prod.properties`)
  - `test`: H2 em memoria com `ddl-auto=create-drop` (`application-test.properties`)

## Como executar

Windows:

```bash
cd battleship_api/battleship
.\mvnw.cmd "spring-boot:run" "-Dspring-boot.run.profiles=dev"
.\mvnw.cmd "spring-boot:run" "-Dspring-boot.run.profiles=prod"
```

Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw spring-boot:run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Observacao: o profile `dev` sobe a API sem banco de dados; o profile `prod` espera PostgreSQL disponivel com as variaveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` opcionalmente definidas. O profile `test` usa H2 em memoria.

Servidor: http://localhost:8080

## REST API

Base path: /api/game

### Listar jogos disponiveis

- Metodo: GET /api/game/available

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

