import random
from front.barcos.barco_base import Barco

class Tabuleiro:
    def __init__(self, linhas, colunas):
        self.linhas = linhas
        self.colunas = colunas
        self.barcos = []

    def posicao_valida(self, barco: Barco):
        """Verifica se o barco cabe no tabuleiro e não sobrepõe outro barco."""
        for (x, y) in barco.get_posicoes():
            if x < 0 or y < 0 or x >= self.colunas or y >= self.linhas:
                return False

            for b in self.barcos:
                if (x, y) in b.get_posicoes():
                    return False
        return True

    def adicionar_barco(self, barco: Barco):
        """Adiciona o barco se a posição for válida."""
        if self.posicao_valida(barco):
            self.barcos.append(barco)
            return True
        return False

    def desenhar(self, tela):
        for barco in self.barcos:
            barco.desenhar(tela)

    def posicionar_barcos_automaticamente(self, tipos_barcos):
        """Posiciona automaticamente uma lista de classes de barcos no tabuleiro."""
        for tipo_barco in tipos_barcos:
            colocado = False
            tentativas = 0

            while not colocado and tentativas < 200:  # evita loop infinito
                horizontal = random.choice([True, False])

                if horizontal:
                    x = random.randint(0, self.colunas - tipo_barco().tamanho)
                    y = random.randint(0, self.linhas - 1)
                else:
                    x = random.randint(0, self.colunas - 1)
                    y = random.randint(0, self.linhas - tipo_barco().tamanho)

                barco = tipo_barco(x, y, horizontal)

                if self.adicionar_barco(barco):
                    colocado = True
                else:
                    tentativas += 1

            if not colocado:
                print(f" foi possível posicionar '{tipo_barco().nome}' após {tentativas} tentativas.")
