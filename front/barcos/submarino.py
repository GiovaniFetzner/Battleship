from .barco_base import Barco, carregar_imagem

class Submarino(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        imagem = carregar_imagem("submarino.png", 3, horizontal)
        super().__init__("Submarino", 3, (255, 255, 0), x, y, horizontal, imagem)