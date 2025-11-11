from .barco_base import Barco, carregar_imagem

class PortaAvioes(Barco):
    def __init__(self, x=0, y=0, horizontal=True):
        super().__init__("Porta-Aviões", 5, (0, 0, 255), x, y, horizontal)
        self.imagem, self.offset_x, self.offset_y = carregar_imagem("porta_avioes.png", 5, horizontal)
