# Usa imagem oficial Python
FROM python:3.11-slim

# Define diretório de trabalho
WORKDIR /app

# Copia o conteúdo do projeto
COPY . .

# Instala dependências (se houver requirements.txt)
# RUN pip install -r requirements.txt

# Define variável de ambiente para o modo docker
ENV MODO_EXEC=docker

# Porta UDP e TCP expostas
EXPOSE 5000/udp
EXPOSE 5001/tcp

# Executa o jogador
CMD ["python", "jogador.py"]
