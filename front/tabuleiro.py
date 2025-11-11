import random
from front.barcos.barco_base import Barco

class Tabuleiro:
    def __init__(self, linhas, colunas):
        self.linhas = linhas
        self.colunas = colunas
        self.barcos = []                 # lista de instâncias de barcos
        self.tiros_recebidos = set()     # {(x, y)} de tiros que já foram feitos

    # --- POSICIONAMENTO ---
    def posicao_valida(self, barco: Barco):
        for (x, y) in barco.get_posicoes():
            if x < 0 or y < 0 or x >= self.colunas or y >= self.linhas:
                return False
            for b in self.barcos:
                if (x, y) in b.get_posicoes():
                    return False
        return True

    def adicionar_barco(self, barco: Barco):
        if self.posicao_valida(barco):
            self.barcos.append(barco)
            return True
        return False

    def posicionar_barcos_automaticamente(self, tipos_barcos):
        for tipo_barco in tipos_barcos:
            colocado = False
            tentativas = 0

            while not colocado and tentativas < 200:
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
                print(f" Não foi possível posicionar '{tipo_barco().nome}' após {tentativas} tentativas.")

    # --- LÓGICA DE ATAQUE ---
    def receber_tiro(self, x, y):
        """Retorna 'hit', 'destroyed' ou 'miss'."""
        if (x, y) in self.tiros_recebidos:
            return "repeat"  # já foi atingido antes

        self.tiros_recebidos.add((x, y))

        for barco in self.barcos:
            if (x, y) in barco.get_posicoes():
                barco.registrar_atingido(x, y)
                if barco.destruido():
                    return "destroyed"
                else:
                    return "hit"
        return "miss"

    def perdeu(self):
        """Retorna True se todos os barcos foram destruídos."""
        return all(barco.destruido() for barco in self.barcos)
    
    def todos_destruidos(self):
        """Retorna True se todos os barcos do tabuleiro foram destruídos."""
        return all(barco.destruido for barco in self.barcos)


    # --- VISUAL ---
    def desenhar(self, tela):
        for barco in self.barcos:
            barco.desenhar(tela)


