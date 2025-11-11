from .barco_base import Barco, carregar_imagem

class Bombardeiro(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        super().__init__("Bombardeiro", 4, (255, 0, 0), x, y, horizontal)
        self.imagem, self.offset_x, self.offset_y = carregar_imagem("bombardeiro.png", 4, horizontal)
