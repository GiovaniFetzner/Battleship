from .barco_base import Barco, carregar_imagem

class Submarino(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        super().__init__("Submarino", 3, (0, 255, 0), x, y, horizontal)
        self.imagem, self.offset_x, self.offset_y = carregar_imagem("submarino.png", 3, horizontal)
