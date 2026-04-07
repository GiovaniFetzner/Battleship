# Battleship

Projeto de Batalha Naval com backend em Spring Boot e frontend web em JavaScript puro.

## Documentacao do projeto

- Backend: [battleship_api/battleship/README.md](battleship_api/battleship/README.md)
- Frontend: [battleship_app/README.md](battleship_app/README.md)

## Estrutura

```text
battleship/
|- battleship_api/
|  |- battleship/          # API Spring Boot
|- battleship_app/         # App web HTML/CSS/JS
|- README.md               # Visao geral
```

## Stack

- Backend: Java 17, Spring Boot 3.2.2, Maven, WebSocket
- Frontend: HTML5, CSS3, JavaScript (vanilla), WebSocket API

## Funcionalidades atuais

- Criacao de sala e entrada de segundo jogador via REST
- Fases da partida: espera, posicionamento, batalha e fim
- Posicionamento de navios no cliente e envio ao backend
- Sinalizacao de jogador pronto
- Ataques em tempo real com retorno de HIT/MISS via WebSocket
- HUD com estado da partida, turno, navios e placar de ataques

## Como executar

### Opcao 1: Com Docker (Recomendado para produção)

```bash
# Iniciar PostgreSQL + API
docker-compose up -d

# Frontend
cd battleship_app
npm install
npm run dev
```

Backend: http://localhost:8080
Frontend: http://localhost:3000


### Opcao 2: Sem Docker (Desenvolvimento local)

#### 1. Subir backend

Windows:

```bash
cd battleship_api/battleship
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend: http://localhost:8080

#### 2. Subir frontend

```bash
cd battleship_app
npm install
npm run dev
```

Frontend: http://localhost:3000

## Estado atual

- Projeto funcional para fluxo completo de partida local (2 jogadores)
- Persistencia em memoria no backend (sem banco de dados)
- Foco em estudo de dominio, regras de jogo e comunicacao em tempo real
