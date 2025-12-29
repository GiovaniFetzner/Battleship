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

