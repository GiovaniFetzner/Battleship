from .barco_base import Barco, carregar_imagem

class Bombardeiro(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        imagem = carregar_imagem("bombardeiro.png", 4, horizontal)
        super().__init__("Bombardeiro", 4, (0, 255, 0), x, y, horizontal, imagem)
