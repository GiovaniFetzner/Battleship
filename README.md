# Battleship

[![API CI/CD](https://github.com/GiovaniFetzner/Battleship/actions/workflows/api-ci.yml/badge.svg)](https://github.com/GiovaniFetzner/Battleship/actions/workflows/api-ci.yml)

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

### Opcao 1: Desenvolvimento Local (Recomendado)

#### 1. Subir infra com Docker (PostgreSQL + Redis)

```bash
# Iniciar apenas banco de dados e cache
docker-compose up -d
```

Servicos iniciados:
- PostgreSQL: localhost:5432
- Redis: localhost:6379

#### 2. Subir backend

Windows:

```bash
cd battleship_api/battleship
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Linux/macOS:

```bash
cd battleship_api/battleship
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend: http://localhost:8080

#### 3. Subir frontend

```bash
cd battleship_app
npm install
npm run dev
```

Frontend: http://localhost:3000

### Opcao 2: Deploy Completo em Docker (Staging/Producao)

```bash
# Build e inicia todos os containers (API + Frontend + Infra)
docker-compose -f docker-compose.prod.yml up -d
```

Servicos iniciados:
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- Backend API: http://localhost:8080
- Frontend: http://localhost:80

**Nota sobre rebuild:** A cada execucao do comando acima, novas imagens serao buildadas automaticamente. Remova a secao `build:` e use `image:` para usar versoes fixas.

### [Em revisão] - Opcao 3: AWS (Producao)

Para deploy em AWS Beanstalk, ECS ou App Runner:

1. **Copiar imagens para ECR (Elastic Container Registry):**
   ```bash
   aws ecr create-repository --repository-name battleship-api --region us-east-1
   aws ecr create-repository --repository-name battleship-frontend --region us-east-1
   
   # Build e push
   docker build -t <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/battleship-api:latest ./battleship_api/battleship
   docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/battleship-api:latest
   
   docker build -t <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/battleship-frontend:latest ./battleship_app
   docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/battleship-frontend:latest
   ```

2. **Configurar servicos gerenciados:**
   - RDS PostgreSQL (substituir container `postgres`)
   - ElastiCache Redis (substituir container `redis`)

3. **Definir variaveis de ambiente na API:**
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/battleship`
   - `DB_USERNAME=<usuario>`
   - `DB_PASSWORD=<senha>` (via Secrets Manager)
   - `WEBSOCKET_ALLOWED_ORIGINS=https://seu-dominio.com`

Para detalhes completos, consulte [AWS_DEPLOY.md](AWS_DEPLOY.md).

## Rebuild Forçado (Dev)

Quando você modifica o código backend ou frontend, é necessário forçar rebuild das imagens:

### Opcao 1: Rebuild durante inicializacao

```bash
# Dev - Apenas infra (sem rebuild, use modo local)
docker-compose up -d

# Prod - Rebuild automatico de todos os containers
docker-compose -f docker-compose.prod.yml up -d --build
```

### Opcao 2: Rebuild explícito (sem cache)

```bash
# Rebuild sem cache (mais lento, mas garante limpeza completa)
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d
```

### Opcao 3: Rebuild de serviço especifico

```bash
# Rebuild apenas da API
docker-compose -f docker-compose.prod.yml build --no-cache battleship-api
docker-compose -f docker-compose.prod.yml up -d battleship-api

# Rebuild apenas do Frontend
docker-compose -f docker-compose.prod.yml build --no-cache battleship-frontend
docker-compose -f docker-compose.prod.yml up -d battleship-frontend
```

**Dica:** Em desenvolvimento local (Opcao 1), execute backend e frontend sem Docker para feedback imediato e não precisar reconstruir containers.

## Estado atual

- Projeto funcional para fluxo completo de partida local (2 jogadores)
- Persistencia com PostgreSQL + Redis (containers em dev, servicos gerenciados em prod)
- Suporte a deploy em AWS (Beanstalk/ECS/AppRunner)
- Foco em estudo de dominio, regras de jogo e comunicacao em tempo real


