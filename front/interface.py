import pygame

pygame.init()

# -----------------------------
# Configurações principais
# -----------------------------
TAMANHO_CELULA = 40
LINHAS = 10
COLUNAS = 10
LARGURA_GRID = COLUNAS * TAMANHO_CELULA
ALTURA_GRID = LINHAS * TAMANHO_CELULA

LARGURA_SIDEBAR = 450
LARGURA_TELA = LARGURA_GRID + LARGURA_SIDEBAR
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
COR_BOTAO = (70, 80, 120)
COR_BOTAO_HOVER = (100, 110, 160)

# -----------------------------
# Fontes
# -----------------------------
pygame.font.init()
FONTE_TITULO = pygame.font.SysFont("arial", 26, bold=True)
FONTE_TEXTO = pygame.font.SysFont("arial", 20, bold=False)
FONTE_LOG = pygame.font.SysFont("consolas", 16, bold=False)

# -----------------------------
# Classe Interface
# -----------------------------
class Interface:
    def __init__(self):
        self.log_mensagens = []
        self.jogadores = []
        self.scroll_offset = 0
        self.max_log_lines = 50
        self.altura_log = 150
        self.area_log = pygame.Rect(0, ALTURA_TELA - self.altura_log, LARGURA_GRID, self.altura_log)

        # Botão de saída
        largura_botao = 180
        altura_botao = 50
        x_botao = LARGURA_GRID + (LARGURA_SIDEBAR - largura_botao) // 2
        y_botao = ALTURA_TELA - 80
        self.botao_sair = pygame.Rect(x_botao, y_botao, largura_botao, altura_botao)
        self.hover_sair = False

    # --- LOG ---
    def adicionar_log(self, mensagem):
        self.log_mensagens.append(mensagem)
        print(mensagem)
        if len(self.log_mensagens) > self.max_log_lines:
            self.log_mensagens.pop(0)

        linhas_visiveis = 7
        max_offset = max(0, len(self.log_mensagens) - linhas_visiveis)
        if self.scroll_offset >= max_offset - 1:
            self.scroll_offset = max_offset

    def mover_scroll(self, direcao):
        linhas_visiveis = 7
        max_offset = max(0, len(self.log_mensagens) - linhas_visiveis)
        self.scroll_offset = max(0, min(self.scroll_offset + direcao, max_offset))

    def mouse_sobre_log(self, pos_mouse):
        return self.area_log.collidepoint(pos_mouse)

    def desenhar_log(self, tela):
        pygame.draw.rect(tela, COR_LOG_BG, self.area_log)

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

        total_msgs = len(self.log_mensagens)
        if total_msgs > linhas_visiveis:
            proporcao = linhas_visiveis / total_msgs
            altura_scroll = max(20, int(proporcao * (self.altura_log - 20)))
            deslocamento_max = (self.altura_log - 20) - altura_scroll

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
        self.jogadores = lista_jogadores

    def desenhar_hud(self, tela):
        sidebar_rect = pygame.Rect(LARGURA_GRID, 0, LARGURA_SIDEBAR, ALTURA_TELA)
        pygame.draw.rect(tela, COR_SIDEBAR, sidebar_rect)

        titulo = FONTE_TITULO.render("Painel do Jogo", True, COR_TEXTO)
        tela.blit(titulo, (LARGURA_GRID + 20, 20))

        pygame.draw.line(
            tela, (100, 100, 120), (LARGURA_GRID + 15, 60), (LARGURA_TELA - 15, 60), 1
        )

        y = 90
        for j in self.jogadores:
            nome = FONTE_TITULO.render(f"{j['nome']}", True, COR_TEXTO)
            acertos = FONTE_TEXTO.render(f"Acertos: {j['acertos']}", True, COR_TEXTO)
            tela.blit(nome, (LARGURA_GRID + 25, y))
            tela.blit(acertos, (LARGURA_GRID + 25, y + 35))
            y += 110

        # --- Botão SAIR ---
        cor = COR_BOTAO_HOVER if self.hover_sair else COR_BOTAO
        pygame.draw.rect(tela, cor, self.botao_sair, border_radius=10)

        texto = FONTE_TITULO.render("Sair", True, (255, 255, 255))
        texto_rect = texto.get_rect(center=self.botao_sair.center)
        tela.blit(texto, texto_rect)

    # --- Interação do botão ---
    def checar_clique(self, pos_mouse):
        """Retorna True se o botão 'Sair' for clicado."""
        return self.botao_sair.collidepoint(pos_mouse)

    def atualizar_hover(self, pos_mouse):
        """Atualiza o estado de hover do botão."""
        self.hover_sair = self.botao_sair.collidepoint(pos_mouse)
