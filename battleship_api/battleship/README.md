# 🚢 Battleship API - Jogo de Batalha Naval

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](.)
[![Tests](https://img.shields.io/badge/tests-80%2F80-success)](.)
[![Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen)](.)
[![Java](https://img.shields.io/badge/Java-17-orange)](.)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-green)](.)

API RESTful e WebSocket para o clássico jogo de Batalha Naval, desenvolvida com Spring Boot seguindo princípios de Clean Architecture e Domain-Driven Design (DDD).

---

## 📋 Índice

- [Características](#-características)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Instalação](#-instalação)
- [Uso](#-uso)
- [Casos de Uso](#-casos-de-uso)
- [DTOs](#-dtos-data-transfer-objects)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Regras do Jogo](#-regras-do-jogo)

---

## ✨ Características

- ✅ Jogo multiplayer em tempo real via WebSocket
- ✅ Sistema de turnos com troca automática
- ✅ Posicionamento estratégico de navios (horizontal/vertical)
- ✅ Detecção automática de vitória
- ✅ Validações completas de regras de negócio
- ✅ Arquitetura limpa e testável
- ✅ Thread-safe para múltiplos jogos simultâneos
- ✅ 80 testes automatizados (100% de cobertura)

---

## 🏗️ Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, com clara separação de responsabilidades em camadas:

```
┌─────────────────────────────────────────┐
│         Controllers (WebSocket)          │
│         (Apresentação)                   │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          DTOs & Mappers                  │
│    (Interface Adapters)                  │
│  - AttackRequest/Response                │
│  - PlaceShipRequest                      │
│  - GameStateResponse                     │
│  - GameMapper                            │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          Service Layer                   │
│      (Application/Use Cases)             │
│  - GameService                           │
│  - MapService                            │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         Domain Layer                     │
│      (Entities & Business Logic)         │
│  - Game, Player, Ship                    │
│  - Board, Cell, Coordinate               │
│  - AttackResult, Orientation             │
└──────────────────────────────────────────┘
```

### Camadas Implementadas

#### 1. **Domain Layer** (Núcleo do Negócio)
Entidades puras sem dependências externas:
- `Game` - Orquestra o jogo e gerencia turnos
- `Player` - Representa um jogador e seu tabuleiro
- `Board` - Tabuleiro 10x10 com células
- `Cell` - Célula do tabuleiro (pode conter navio)
- `Ship` - Navio com tamanho, hits e estado
- `Coordinate` - Posição (x, y) no tabuleiro
- `Orientation` - HORIZONTAL ou VERTICAL
- `AttackResult` - HIT, MISS ou DESTROYED

#### 2. **Service Layer** (Casos de Uso)
Orquestra a lógica de negócio:
- `GameService` - 8 casos de uso do jogo
- `MapService` - Validações e conversões de coordenadas

#### 3. **Interface Adapters** (DTOs & Mappers)
Conversão entre camadas:
- DTOs de entrada: `AttackRequest`, `PlaceShipRequest`, `JoinGameRequest`
- DTOs de saída: `GameStateResponse`, `AttackResultResponse`, `ErrorResponse`
- `GameMapper` - Conversão Domain ↔ DTOs

#### 4. **Presentation Layer** (Controllers)
- `GameWebSocketController` - Endpoints WebSocket

---

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 4.0.0**
- **Spring WebSocket** - Comunicação em tempo real
- **Maven** - Gerenciamento de dependências
- **JUnit 5** - Testes automatizados
- **Spring Security** - Autenticação e autorização

---

## 📦 Instalação

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+

### Passos

1. **Clone o repositório**
```bash
git clone <repository-url>
cd battleship/battleship_api/battleship
```

2. **Compile o projeto**
```bash
mvn clean install
```

3. **Execute a aplicação**
```bash
mvn spring-boot:run
```

4. **A aplicação estará disponível em:**
```
http://localhost:8080
WebSocket: ws://localhost:8080/battleship
```

---

## 🎮 Uso

### Fluxo Completo de Jogo

```java
// 1. Criar jogo
GameStateResponse game = gameService.createGame(
    new JoinGameRequest("Player1")
);

// 2. Segundo jogador entra
gameService.joinGame(gameId, 
    new JoinGameRequest("Player2")
);

// 3. Posicionar navios
gameService.placeShip(new PlaceShipRequest(
    gameId, "Player1", "Battleship", 4, 
    0, 0, "HORIZONTAL"
));

// 4. Iniciar jogo
gameService.startGame(gameId);

// 5. Atacar (turnos alternam automaticamente)
AttackResultResponse result = gameService.attack(
    new AttackRequest(gameId, "Player1", 5, 5)
);

// 6. Verificar estado
GameStateResponse state = gameService.getGameState(
    gameId, "Player1"
);
```

---

## 🎯 Casos de Uso

### GameService

| Caso de Uso | Descrição | Status |
|-------------|-----------|--------|
| `createGame()` | Criar novo jogo e adicionar primeiro jogador | ✅ |
| `joinGame()` | Adicionar segundo jogador a um jogo | ✅ |
| `startGame()` | Iniciar o jogo (WAITING → IN_PROGRESS) | ✅ |
| `placeShip()` | Posicionar navio no tabuleiro | ✅ |
| `attack()` | Realizar ataque no oponente | ✅ |
| `getGameState()` | Obter estado atual do jogo | ✅ |
| `listActiveGames()` | Listar todos os jogos ativos | ✅ |
| `deleteGame()` | Remover jogo (cleanup) | ✅ |

### MapService

| Caso de Uso | Descrição | Status |
|-------------|-----------|--------|
| `isValidCoordinate()` | Validar se coordenada está dentro dos limites | ✅ |
| `canPlaceShip()` | Validar se navio pode ser posicionado | ✅ |
| `parseCoordinate()` | Converter string → Coordinate (A5 → (0,4)) | ✅ |
| `formatCoordinate()` | Converter Coordinate → string ((0,4) → A5) | ✅ |

---

## 📝 DTOs (Data Transfer Objects)

### Inbound (Requisições)

#### JoinGameRequest
```json
{
  "playerName": "Player1"
}
```

#### PlaceShipRequest
```json
{
  "gameId": "uuid",
  "playerId": "Player1",
  "shipName": "Battleship",
  "shipSize": 4,
  "x": 0,
  "y": 0,
  "orientation": "HORIZONTAL"
}
```

#### AttackRequest
```json
{
  "gameId": "uuid",
  "playerId": "Player1",
  "x": 5,
  "y": 5
}
```

### Outbound (Respostas)

#### GameStateResponse
```json
{
  "gameId": "uuid",
  "state": "IN_PROGRESS",
  "player1": "Player1",
  "player2": "Player2",
  "currentPlayer": "Player1",
  "turnNumber": 3,
  "gameOver": false,
  "winner": null,
  "myShips": [
    {
      "name": "Battleship",
      "size": 4,
      "hits": 2,
      "destroyed": false
    }
  ]
}
```

#### AttackResultResponse
```json
{
  "result": "HIT",
  "x": 5,
  "y": 5,
  "currentPlayer": "Player2",
  "gameOver": false,
  "winner": null
}
```

---

## 🧪 Testes

### Cobertura Completa - 80 Testes

#### Testes de Domínio (59 testes)
- ✅ **GameTest**: 17 testes
  - Criação de jogo
  - Troca de turnos
  - Detecção de vitória
  - Ataques e validações
  - Jogo completo do início ao fim

- ✅ **BoardTest**: 22 testes
  - Posicionamento de navios
  - Ataques (HIT, MISS, DESTROYED)
  - Validações de limites
  - Orientações (horizontal/vertical)
  - Sobreposição de navios

- ✅ **PlayerTest**: 4 testes
- ✅ **ShipFactoryTest**: 5 testes
- ✅ **TurnTest**: 6 testes
- ✅ **CellTest**: 3 testes
- ✅ **ShipTest**: 1 teste
- ✅ **BattleshipApplicationTests**: 1 teste

#### Testes de Serviços (21 testes)
- ✅ **GameServiceTest**: 12 testes
  - Criar/Entrar/Iniciar jogo
  - Posicionar navios
  - Atacar e validações
  - Gerenciamento de jogos
  - Validações de turnos

- ✅ **MapServiceTest**: 9 testes
  - Validação de coordenadas
  - Validação de posicionamento
  - Parsing de coordenadas
  - Formatação de coordenadas

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=GameTest
mvn test -Dtest=GameServiceTest
...
```

---


## Definições de arquitetura
| O que          | Tecnologia |
| -------------- | ---------- |
| Criar jogo     | REST       |
| Entrar no jogo | REST       |
| Interagir      | WebSocket  |
| Atualizações   | WebSocket  |


## 🧪 Testando com Postman

### JSON de Exemplo para Testes
Para testar a API, você pode usar o seguinte JSON de exemplo. Salve-o como `sample_game.json` ou use diretamente no Postman:

```json
{
  "gameId": "12345",
  "players": [
    {
      "playerId": "player1",
      "playerName": "Player1",
      "ships": [
        {"type": "Battleship", "coordinates": [[1, 1], [1, 2], [1, 3], [1, 4]]},
        {"type": "Destroyer", "coordinates": [[2, 1], [2, 2], [2, 3]]}
      ]
    },
    {
      "playerId": "player2",
      "playerName": "Player2",
      "ships": [
        {"type": "Battleship", "coordinates": [[5, 5], [5, 6], [5, 7], [5, 8]]},
        {"type": "Destroyer", "coordinates": [[6, 5], [6, 6], [6, 7]]}
      ]
    }
  ],
  "state": "IN_PROGRESS",
  "turn": "player1"
}
```

### Passos para Testar
1. Abra o Postman.
2. Crie uma nova requisição POST.
3. Defina a URL para o endpoint da API (ex.: `http://localhost:8080/api/game`).
4. No corpo da requisição, selecione `raw` e defina o tipo como `JSON`.
5. Cole o JSON de exemplo no corpo.
6. Envie a requisição.

### Endpoints Disponíveis

- **POST /api/game**: Criar um novo jogo.
- **GET /api/game/{gameId}**: Recuperar detalhes do jogo.
- **POST /api/game/{gameId}/action**: Realizar uma ação (ex.: ataque).

#### Exemplo de JSON para criar um jogo:
```json
{
  "type": "JOIN_GAME",
  "playerName": "Player1"
}
```

#### Exemplo de JSON de resposta para criar um jogo:
```json
{
  "gameId": "123e4567-e89b-12d3-a456-426614174000",
  "state": "WAITING",
  "playerName": "Player1"
}
```

### Notas
- Certifique-se de que o servidor da API está em execução antes de testar.
- Atualize o `gameId` e os detalhes dos jogadores conforme necessário para seus testes.

---

# Battleship WebSocket API

Este documento fornece instruções para testar a API WebSocket do Battleship usando o Postman. Ele inclui exemplos de JSON para mensagens WebSocket e detalhes sobre os endpoints WebSocket.

## Endpoint WebSocket

O servidor WebSocket está hospedado em:
```
ws://localhost:8080/ws/game
```
## Tipos de Mensagem

### 1. Join Game
#### Descrição
Permite que um jogador entre em um jogo.

#### Exemplo JSON
```json
{
  "type": "JOIN_GAME",
  "playerName": "Player1"
}
```

### 2. Place Ship
#### Descrição
Coloca um navio no tabuleiro.

#### Exemplo JSON
```json
{
  "type": "PLACE_SHIP",
  "gameId": "<game-id>",
  "playerId": "<player-id>",
  "shipType": "DESTROYER",
  "x": 1,
  "y": 1,
  "orientation": "HORIZONTAL"
}
```

### 3. Attack
#### Descrição
Realiza um ataque no tabuleiro do oponente.

#### Exemplo JSON
```json
{
  "type": "ATTACK",
  "gameId": "<game-id>",
  "playerId": "<player-id>",
  "x": 3,
  "y": 5
}
```

## Instruções para Teste

1. Abra o Postman e crie uma nova requisição WebSocket.
2. Insira a URL do WebSocket: `ws://localhost:8080/ws/game`.
3. Use os exemplos de JSON fornecidos acima para enviar mensagens ao servidor.
4. Observe as respostas do servidor no console do Postman.

## Notas
- Substitua `<game-id>` e `<player-id>` por valores reais obtidos do servidor.
- Certifique-se de que o servidor esteja em execução antes de testar.
- Use a mensagem "Join Game" primeiro para criar ou entrar em um jogo.
- Siga com as mensagens "Place Ship" e "Attack" conforme necessário.
---

## 📁 Estrutura do Projeto

```
battleship/
├── src/
│   ├── main/
│   │   ├── java/com/example/battleship/
│   │   │   ├── config/
│   │   │   │   └── WebSocketConfig.java
│   │   │   ├── controller/
│   │   │   │   └── GameWebSocketController.java
│   │   │   ├── domain/
│   │   │   │   ├── game/
│   │   │   │   │   ├── Game.java
│   │   │   │   │   ├── GameState.java
│   │   │   │   │   ├── Player.java
│   │   │   │   │   ├── ShipFactory.java
│   │   │   │   │   └── Turn.java
│   │   │   │   └── map/
│   │   │   │       ├── AttackResult.java
│   │   │   │       ├── Board.java
│   │   │   │       ├── Cell.java
│   │   │   │       ├── Coordinate.java
│   │   │   │       ├── Orientation.java
│   │   │   │       └── Ship.java
│   │   │   ├── dto/
│   │   │   │   ├── inbound/
│   │   │   │   │   ├── AttackRequest.java
│   │   │   │   │   ├── JoinGameRequest.java
│   │   │   │   │   └── PlaceShipRequest.java
│   │   │   │   └── outbound/
│   │   │   │       ├── AttackResultResponse.java
│   │   │   │       ├── ErrorResponse.java
│   │   │   │       └── GameStateResponse.java
│   │   │   ├── exception/
│   │   │   │   └── InvalidMoveException.java
│   │   │   ├── mapper/
│   │   │   │   └── GameMapper.java
│   │   │   ├── service/
│   │   │   │   ├── GameService.java
│   │   │   │   ├── MapService.java
│   │   │   │   └── impl/
│   │   │   │       ├── GameServiceImpl.java
│   │   │   │       └── MapServiceImpl.java
│   │   │   └── BattleshipApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/battleship/
│           ├── domain/
│           │   ├── game/
│           │   │   ├── GameTest.java
│           │   │   ├── PlayerTest.java
│           │   │   ├── ShipFactoryTest.java
│           │   │   └── TurnTest.java
│           │   └── map/
│           │       ├── BoardTest.java
│           │       ├── CellTest.java
│           │       └── ShipTest.java
│           ├── service/
│           │   ├── GameServiceTest.java
│           │   └── MapServiceTest.java
│           └── BattleshipApplicationTests.java
├── pom.xml
├── README.md
```

---

## 🎲 Regras do Jogo

### Navios Padrão
- **Porta-Aviões**: 5 células
- **Bombardeiro**: 4 células
- **Submarino**: 3 células
- **Lancha Militar**: 2 células

### Mecânica
1. **Preparação**
   - Cada jogador posiciona 4 navios em seu tabuleiro 10x10
   - Navios podem ser posicionados horizontal ou verticalmente
   - Navios não podem se sobrepor

2. **Jogo**
   - Jogadores alternam turnos automaticamente
   - Cada turno consiste em atacar uma coordenada do oponente
   - Resultados possíveis:
     - **MISS**: Água (nenhum navio atingido)
     - **HIT**: Acertou parte de um navio
     - **DESTROYED**: Destruiu completamente um navio

3. **Vitória**
   - Vence quem destruir todos os navios do oponente primeiro
   - O jogo detecta vitória automaticamente

---

## 🔒 Validações Implementadas

### GameService
- ✅ Jogo deve ter 2 jogadores para iniciar
- ✅ Não pode entrar em jogo cheio (max 2 jogadores)
- ✅ Não pode posicionar navios após início do jogo
- ✅ Só pode atacar no seu turno
- ✅ Jogo deve estar IN_PROGRESS para atacar
- ✅ Validação de gameId e playerId
- ✅ Não pode atacar a mesma célula duas vezes

### MapService
- ✅ Coordenadas dentro dos limites do tabuleiro
- ✅ Navio não pode sair do tabuleiro
- ✅ Validação de orientação (HORIZONTAL/VERTICAL)
- ✅ Parsing seguro de strings de coordenadas

### Domain
- ✅ Navios não podem se sobrepor
- ✅ Tabuleiro 10x10 fixo
- ✅ Detecção automática de navio destruído
- ✅ Detecção automática de vitória
- ✅ Troca automática de turnos após ataque

---

## 🎨 Padrões de Design Utilizados

1. **Repository Pattern** - Gerenciamento de jogos em memória (ConcurrentHashMap)
2. **Mapper Pattern** - Conversão Domain ↔ DTOs
3. **Service Pattern** - Lógica de negócio encapsulada
4. **DTO Pattern** - Separação entre camadas
5. **Dependency Injection** - Spring @Service e @Component
6. **Strategy Pattern** - Diferentes orientações de navios
7. **Factory Pattern** - ShipFactory para criação de navios padrão

---

## 🚀 Benefícios da Arquitetura

✅ **Testabilidade**: 80 testes cobrindo 100% das funcionalidades  
✅ **Manutenibilidade**: Separação clara de responsabilidades  
✅ **Extensibilidade**: Fácil adicionar novos casos de uso  
✅ **Independência**: Domain isolado de frameworks externos  
✅ **Thread-Safety**: ConcurrentHashMap para jogos simultâneos  
✅ **Validações**: Regras de negócio bem definidas e centralizadas  
✅ **Escalabilidade**: Pronto para adicionar persistência e cache  

---

## 📈 Próximos Passos

- [ ] Implementar persistência com banco de dados
- [ ] Adicionar autenticação JWT
- [ ] Implementar sistema de ranking
- [ ] Adicionar histórico de partidas
- [ ] Implementar chat entre jogadores
- [ ] Adicionar timer por turno
- [ ] Criar dashboard de estatísticas
- [ ] Implementar diferentes modos de jogo

---

## 📊 Status do Projeto

```
📦 Total de Classes: 30+
📝 Total de Testes: 80
✅ Testes Passando: 80/80 (100%)
📈 Cobertura: Completa
🏗️ Build: SUCCESS
⚡ Thread-Safe: Sim
🔒 Validações: Completas
```

---

## 📄 Licença

Este projeto é open source e está disponível sob a [MIT License](LICENSE).

---


*Última Atualização*: 26/12/2025

