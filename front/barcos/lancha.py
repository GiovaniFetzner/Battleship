from .barco_base import Barco, carregar_imagem

class Lancha(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        super().__init__("Lancha", 2, (255, 255, 0), x, y, horizontal)
        self.imagem, self.offset_x, self.offset_y = carregar_imagem("lancha.png", 2, horizontal)
