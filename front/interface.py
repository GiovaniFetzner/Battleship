import pygame

pygame.init()

# -----------------------------
# Configurações principais
# -----------------------------
TAMANHO_CELULA = 40  # era 50 → reduz para caber
LINHAS = 10
COLUNAS = 10
LARGURA_GRID = COLUNAS * TAMANHO_CELULA
ALTURA_GRID = LINHAS * TAMANHO_CELULA

LARGURA_SIDEBAR = 450
LARGURA_TELA = LARGURA_GRID + LARGURA_SIDEBAR

# Diminui a área do tabuleiro, mas mantém o log visível
ALTURA_TELA = ALTURA_GRID + 150  # 150px extras pro log


# -----------------------------
# Cores
# -----------------------------
COR_FUNDO = (20, 25, 35)
COR_GRID = (90, 100, 120)
COR_SIDEBAR = (35, 40, 55)
COR_TEXTO = (235, 235, 240)
COR_LOG_BG = (30, 30, 40)
COR_SCROLL = (80, 80, 100)

# -----------------------------
# Fontes
# -----------------------------
pygame.font.init()
FONTE_TITULO = pygame.font.SysFont("arial", 26, bold=True)
FONTE_TEXTO = pygame.font.SysFont("arial", 20, bold=False)
FONTE_LOG = pygame.font.SysFont("consolas", 16, bold=False)  # menor e monoespaçada

# -----------------------------
# Classe Interface
# -----------------------------
class Interface:
    def __init__(self):
        self.log_mensagens = []
        self.jogadores = []
        self.scroll_offset = 0  # controle do scroll vertical do log
        self.max_log_lines = 50  # quantidade máxima de mensagens guardadas
        self.altura_log = 150    # altura da área do log
        self.area_log = pygame.Rect(0, ALTURA_TELA - self.altura_log, LARGURA_GRID, self.altura_log)

    # --- LOG ---
    def adicionar_log(self, mensagem):
        """Adiciona uma nova mensagem ao log e acompanha automaticamente o final."""
        self.log_mensagens.append(mensagem)
        if len(self.log_mensagens) > self.max_log_lines:
            self.log_mensagens.pop(0)

        # Se o log estiver no fim, acompanhar automaticamente a nova mensagem
        linhas_visiveis = 7
        max_offset = max(0, len(self.log_mensagens) - linhas_visiveis)
        if self.scroll_offset >= max_offset - 1:  # estava no fim
            self.scroll_offset = max_offset


    def mover_scroll(self, direcao):
        """Move o scroll do log (1 = para baixo, -1 = para cima)."""
        linhas_visiveis = 7
        max_offset = max(0, len(self.log_mensagens) - linhas_visiveis)
        self.scroll_offset = max(0, min(self.scroll_offset + direcao, max_offset))

    def mouse_sobre_log(self, pos_mouse):
        """Retorna True se o cursor estiver sobre a área do log."""
        return self.area_log.collidepoint(pos_mouse)

    def desenhar_log(self, tela):
        """Desenha o log na parte inferior do grid com rolagem."""
        pygame.draw.rect(tela, COR_LOG_BG, self.area_log)

        # Área de texto do log
        area_texto = pygame.Rect(10, ALTURA_TELA - self.altura_log + 10, LARGURA_GRID - 30, self.altura_log - 20)
        linhas_visiveis = 7
        inicio = self.scroll_offset
        fim = inicio + linhas_visiveis

        mensagens_visiveis = self.log_mensagens[inicio:fim]

        y = area_texto.y
        for msg in mensagens_visiveis:
            texto = FONTE_LOG.render(msg, True, COR_TEXTO)
            tela.blit(texto, (area_texto.x, y))
            y += 20

        # Scrollbar
        total_msgs = len(self.log_mensagens)
        if total_msgs > linhas_visiveis:
            # proporção do tamanho da barra
            proporcao = linhas_visiveis / total_msgs
            altura_scroll = max(20, int(proporcao * (self.altura_log - 20)))

            # distância total que a barra pode percorrer
            deslocamento_max = (self.altura_log - 20) - altura_scroll

            # posição da barra dentro do espaço do log
            if total_msgs - linhas_visiveis > 0:
                pos_scroll = int((self.scroll_offset / (total_msgs - linhas_visiveis)) * deslocamento_max)
            else:
                pos_scroll = 0

            pygame.draw.rect(
                tela,
                COR_SCROLL,
                (
                    LARGURA_GRID - 15,
                    ALTURA_TELA - self.altura_log + 10 + pos_scroll,
                    8,
                    altura_scroll,
                ),
                border_radius=4,
            )


    # --- HUD ---
    def atualizar_jogadores(self, lista_jogadores):
        """Recebe a lista de jogadores e seus acertos para mostrar no HUD."""
        self.jogadores = lista_jogadores

    def desenhar_hud(self, tela):
        """Desenha o HUD na lateral direita do grid."""
        sidebar_rect = pygame.Rect(LARGURA_GRID, 0, LARGURA_SIDEBAR, ALTURA_TELA)
        pygame.draw.rect(tela, COR_SIDEBAR, sidebar_rect)

        # Título
        titulo = FONTE_TITULO.render("Painel do Jogo", True, COR_TEXTO)
        tela.blit(titulo, (LARGURA_GRID + 20, 20))

        pygame.draw.line(
            tela, (100, 100, 120), (LARGURA_GRID + 15, 60), (LARGURA_TELA - 15, 60), 1
        )

        # Lista de jogadores (HUD lateral)
        y = 90
        for j in self.jogadores:
            nome = FONTE_TITULO.render(f"{j['nome']}", True, COR_TEXTO)
            acertos = FONTE_TEXTO.render(f"Acertos: {j['acertos']}", True, COR_TEXTO)

            tela.blit(nome, (LARGURA_GRID + 25, y))
            tela.blit(acertos, (LARGURA_GRID + 25, y + 35))
            y += 110
