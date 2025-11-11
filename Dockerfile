FROM python:3.12-slim

WORKDIR /app
COPY . /app

CMD ["python", "network/client.py"]
