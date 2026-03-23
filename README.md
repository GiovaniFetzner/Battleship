# Battleship

Projeto de Batalha Naval com backend em Spring Boot e frontend web em JavaScript.

## READMEs do projeto

Este README e o principal. Os outros dois detalham cada parte:

- Backend: [battleship_api/battleship/README.md](battleship_api/battleship/README.md)
- Frontend: [battleship_app/README.md](battleship_app/README.md)

## Estrutura do repositorio

```text
battleship/
|- battleship_api/
|  |- battleship/          # Backend Spring Boot
|- battleship_app/         # Frontend HTML/CSS/JS
|- README.md               # Visao geral
```

## Estado atual

### Backend

- Java 17 + Spring Boot 3.2.2
- REST para criar jogo, entrar no jogo e consultar estado
- WebSocket para eventos em tempo real
- Dominio do jogo implementado e repositorio em memoria
- Testes automatizados passando

### Frontend

- Tela de entrada para criar/entrar em sala
- Tela de jogo com HUD e tabuleiro 10x10
- Posicionamento visual de navios no tabuleiro
- Conexao WebSocket e atualizacao de estado

### Integracao ainda pendente no frontend

- Envio de ataques para o backend via WebSocket
- Envio de posicionamento de navios e sinalizacao de pronto via WebSocket

## Como executar

### 1. Subir backend

```bash
cd battleship_api/battleship
mvnw.cmd spring-boot:run
```

Backend em http://localhost:8080.

### 2. Subir frontend

```bash
cd battleship_app
npm install
npm run dev
```

Frontend em http://localhost:3000.

## Tecnologias

- Backend: Java 17, Spring Boot 3.2.2, Spring Web, Spring WebSocket, Maven, JUnit 5
- Frontend: HTML5, CSS3, JavaScript (vanilla), WebSocket API

## Objetivo

Projeto de estudo e portfolio com foco em:

- modelagem de dominio
- regras de jogo no servidor
- comunicacao em tempo real
- separacao de responsabilidades entre backend e frontend
