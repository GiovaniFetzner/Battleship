FROM python:3.13-slim

WORKDIR /app

# Instalar dependências do sistema para Pygame e rede
RUN apt-get update && apt-get install -y \
    libsdl2-dev \
    libsdl2-image-dev \
    libsdl2-mixer-dev \
    libsdl2-ttf-dev \
    libsdl2-gfx-dev \
    xvfb \
    x11-utils \
    netcat-openbsd \
    iputils-ping \
    && rm -rf /var/lib/apt/lists/*


# Copiar apenas requirements primeiro para aproveitar cache do Docker
COPY requirements.txt /app/

# Instalar dependências Python
RUN pip install --no-cache-dir -r requirements.txt

# Copiar o restante da aplicação
COPY . /app

# Variáveis de ambiente
ENV PLAYER_ID=1
ENV DISPLAY=:99

# Healthcheck mais confiável
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD pgrep -f "python main.py" || exit 1

# Rodar Xvfb em background e depois executar o jogo
CMD sh -c "Xvfb :99 -screen 0 800x950x24 >/dev/null 2>&1 & sleep 2 && exec python main.py ${PLAYER_ID}"
